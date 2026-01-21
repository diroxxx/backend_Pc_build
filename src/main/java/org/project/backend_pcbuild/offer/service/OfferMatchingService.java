package org.project.backend_pcbuild.offer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.project.backend_pcbuild.offer.dto.ComponentOfferDto;
import org.project.backend_pcbuild.offer.model.Brand;
import org.project.backend_pcbuild.pcComponents.model.*;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Service
@Slf4j
public class OfferMatchingService {

    private static final JaroWinklerSimilarity similarity = new JaroWinklerSimilarity();
    
    // Scoring weights
    private static final double WEIGHT_COMMON_WORDS = 0.2;
    private static final double WEIGHT_COMMON_WORDS_GPU = 0.3;
    private static final double WEIGHT_CORES = 0.3;
    private static final double WEIGHT_THREADS = 0.2;
    private static final double WEIGHT_VRAM = 0.5;
    private static final double WEIGHT_CAPACITY = 0.4;
    private static final double WEIGHT_DDR_TYPE = 0.3;
    private static final double WEIGHT_SPEED = 0.3;
    private static final double WEIGHT_CHIPSET = 0.3;
    private static final double WEIGHT_FORMAT = 0.2;
    private static final double WEIGHT_RAM_TYPE = 0.2;
    private static final double WEIGHT_RAM_CAPACITY = 0.2;
    private static final double WEIGHT_SOCKET = 0.4;
    private static final double WEIGHT_POWER = 0.4;
    private static final double WEIGHT_CASE_FORMAT = 0.3;
    
    // Thresholds
    private static final double MIN_ACCEPTABLE_SCORE = 1.0;
    private static final double THRESHOLD_CLOCK_CLOSE = 0.2;
    private static final double THRESHOLD_CLOCK_ACCEPTABLE = 0.5;
    private static final double CLOCK_SCORE_CLOSE = 0.4;
    private static final double CLOCK_SCORE_ACCEPTABLE = 0.2;
    private static final double SCORE_EXACT_MATCH = 1.0;

    // Regex patterns
    private static final Pattern CAPACITY_GB_PATTERN = Pattern.compile("(\\d+)\\s*gb", Pattern.CASE_INSENSITIVE);
    private static final Pattern DDR_TYPE_PATTERN = Pattern.compile("(ddr\\d)", Pattern.CASE_INSENSITIVE);
    private static final Pattern SPEED_MHZ_PATTERN = Pattern.compile("(\\d{3,5})\\s*mhz", Pattern.CASE_INSENSITIVE);
    private static final Pattern POWER_WATT_PATTERN = Pattern.compile("(\\d+)\\s*w", Pattern.CASE_INSENSITIVE);
    private static final Pattern FORMAT_PATTERN = Pattern.compile("\\S*atx|\\S*itx", Pattern.CASE_INSENSITIVE);
    private static final Pattern SOCKET_PATTERN = Pattern.compile("(am4|am5|lga\\d{4}|s\\d{3,4}|fm2|fm1)", Pattern.CASE_INSENSITIVE);
    private static final Pattern CORES_PATTERN = Pattern.compile("(\\d+)\\s*(rdzeni|rdzenie|cores)", Pattern.CASE_INSENSITIVE);
    private static final Pattern THREADS_PATTERN = Pattern.compile("(\\d+)\\s*(wątk|threads)", Pattern.CASE_INSENSITIVE);
    
    // GPU patterns
    private static final Pattern GPU_RTX_PATTERN = Pattern.compile("(rtx\\s*\\d{3,4}(?:\\s*ti|\\s*super)?)");
    private static final Pattern GPU_GTX_PATTERN = Pattern.compile("(gtx\\s*\\d{3,4}(?:\\s*ti)?)");
    private static final Pattern GPU_GT_PATTERN = Pattern.compile("(gt\\s*\\d{3,4})");
    private static final Pattern GPU_RX_PATTERN = Pattern.compile("(rx\\s*\\d{3,4}(?:\\s*xt)?)");
    
    // CPU patterns
    private static final Pattern CPU_INTEL_PATTERN = Pattern.compile("(i[3579])[-\\s]?(\\d{3,5}[a-z0-9]*)");
    private static final Pattern CPU_RYZEN_PATTERN = Pattern.compile("(ryzen\\s*[3579])\\s*(\\d{3,4}[a-z0-9]*)");
    
    // Storage patterns
    private static final Pattern STORAGE_CAPACITY_PATTERN = Pattern.compile("(\\d+)\\s*(gb|tb)", Pattern.CASE_INSENSITIVE);
    
    // Clock speed patterns
    private static final Pattern CLOCK_GHZ_PATTERN = Pattern.compile("(\\d{1,2}[.,]\\d{1,2})\\s*ghz", Pattern.CASE_INSENSITIVE);
    private static final Pattern CLOCK_MHZ_PATTERN = Pattern.compile("(\\d{3,4})\\s*mhz", Pattern.CASE_INSENSITIVE);

    public Optional<Component> matchOfferToComponent(
            ComponentType category,
            ComponentOfferDto offerDto,
            List<?> components) {

        if (components == null || components.isEmpty()) {
            log.debug("No items in DB for category: {}", category);
            return Optional.empty();
        }
        if (offerDto == null || offerDto.getTitle() == null) {
            log.debug("Offer has no title, skipping");
            return Optional.empty();
        }

        OfferContext ctx = new OfferContext(offerDto);
        String[] offerWords = ctx.modelLower.split("\\s+");

        return switch (category) {
            case PROCESSOR     -> matchProcessor(ctx, offerWords, components);
            case GRAPHICS_CARD -> matchGraphicsCard(ctx, offerWords, components);
            case MEMORY        -> matchMemory(ctx, offerWords, components);
            case MOTHERBOARD   -> matchMotherboard(ctx, offerWords, components);
            case POWER_SUPPLY  -> matchPowerSupply(ctx, offerWords, components);
            case STORAGE       -> matchStorage(ctx, offerWords, components);
            case CASE_PC       -> matchCase(ctx, offerWords, components);
            case CPU_COOLER    -> matchCooler(ctx, offerWords, components);
        };
    }

    //** GENERIC MATCHING METHOD **//
    private <T> Optional<Component> findBestMatch(
            OfferContext ctx,
            String[] offerWords,
            List<?> items,
            Function<Object, T> itemExtractor,
            Function<T, Component> componentGetter,
            BiFunction<OfferContext, T, MatchScore> scoreCalculator,
            String categoryName) {

        Component bestComponent = null;
        double bestScore = 0.0;
        String bestDetails = "";

        for (Object obj : items) {
            T item = itemExtractor.apply(obj);
            Component comp = componentGetter.apply(item);
            
            if (comp == null || comp.getModel() == null) continue;
            if (!isBrandInOfferTitle(comp.getBrand(), ctx.title)) continue;

            MatchScore matchScore = scoreCalculator.apply(ctx, item);
            double score = matchScore.totalScore;

            if (score > bestScore) {
                bestScore = score;
                bestComponent = comp;
                bestDetails = matchScore.details;
            }
        }

        if (bestScore < MIN_ACCEPTABLE_SCORE) {
            log.debug("[{} match] No acceptable match for [{}] (best score: {})", 
                     categoryName, ctx.modelRaw, bestScore);
            return Optional.empty();
        }

        if (bestComponent != null) {
            log.debug("[{} match] Best match for [{}] => {} | score={}{}", 
                     categoryName, ctx.modelRaw, bestComponent.getModel(), bestScore, bestDetails);
        }

        return Optional.ofNullable(bestComponent);
    }


    private Optional<Component> matchProcessor(OfferContext ctx, String[] offerWords, List<?> items) {
        String offerCpuClass = extractCpuModel(ctx.modelLower);
        Integer offerCores = extractIntPattern(CORES_PATTERN, ctx.modelLower, 1);
        Integer offerThreads = extractIntPattern(THREADS_PATTERN, ctx.modelLower, 1);
        Double offerBaseClock = extractClockSpeed(ctx.modelLower);

        return findBestMatch(ctx, offerWords, items,
            obj -> (Processor) obj,
            cpu -> cpu.getComponent(),
            (context, cpu) -> {
                Component comp = cpu.getComponent();
                String compModelLower = comp.getModel().toLowerCase();
                String[] compWords = compModelLower.split("\\s+");
                String compCpuClass = extractCpuModel(compModelLower);

                if (offerCpuClass != null && compCpuClass != null && !offerCpuClass.equals(compCpuClass)) {
                    return new MatchScore(0.0, "");
                }

                double wordsScore = calculateCommonWordsScore(offerWords, compWords, WEIGHT_COMMON_WORDS);
                double modelSim = similarity.apply(context.modelLower, compModelLower);
                double coresScore = compareInteger(offerCores, cpu.getCores(), WEIGHT_CORES);
                double threadsScore = compareInteger(offerThreads, cpu.getThreads(), WEIGHT_THREADS);
                
                double clockScore = 0.0;
                if (offerBaseClock != null && cpu.getBaseClock() != null) {
                    double diff = Math.abs(offerBaseClock - cpu.getBaseClock());
                    if (diff < THRESHOLD_CLOCK_CLOSE) {
                        clockScore = CLOCK_SCORE_CLOSE;
                    } else if (diff < THRESHOLD_CLOCK_ACCEPTABLE) {
                        clockScore = CLOCK_SCORE_ACCEPTABLE;
                    }
                }

                double classScore = (offerCpuClass != null && offerCpuClass.equals(compCpuClass)) ? SCORE_EXACT_MATCH : 0.0;
                double total = wordsScore + modelSim + coresScore + threadsScore + clockScore + classScore;
                
                return new MatchScore(total, ", class=" + offerCpuClass);
            },
            "CPU"
        );
    }

    private Optional<Component> matchGraphicsCard(OfferContext ctx, String[] offerWords, List<?> items) {
        String offerChip = extractGpuChip(ctx.modelLower);
        Integer offerVramGb = extractIntPattern(CAPACITY_GB_PATTERN, ctx.modelLower, 1);

        return findBestMatch(ctx, offerWords, items,
            obj -> (GraphicsCard) obj,
            gpu -> gpu.getComponent(),
            (context, gpu) -> {
                Component comp = gpu.getComponent();
                String compModelLower = comp.getModel().toLowerCase();
                String[] compWords = compModelLower.split("\\s+");
                String compChip = extractGpuChip(compModelLower);

                if (offerChip != null && compChip != null && !offerChip.equals(compChip)) {
                    return new MatchScore(0.0, "");
                }

                double wordsScore = calculateCommonWordsScore(offerWords, compWords, WEIGHT_COMMON_WORDS_GPU);
                double modelSim = similarity.apply(context.modelLower, compModelLower);
                double vramScore = compareInteger(offerVramGb, gpu.getVram(), WEIGHT_VRAM);
                double chipScore = (offerChip != null && compChip != null && offerChip.equals(compChip)) ? SCORE_EXACT_MATCH : 0.0;
                double total = wordsScore + modelSim + vramScore + chipScore;
                
                return new MatchScore(total, ", chip=" + offerChip);
            },
            "GPU"
        );
    }

    private Optional<Component> matchMemory(OfferContext ctx, String[] offerWords, List<?> items) {
        Integer offerCapacity = extractIntPattern(CAPACITY_GB_PATTERN, ctx.modelLower, 1);
        String offerType = extractPattern(DDR_TYPE_PATTERN, ctx.modelLower, 1);
        Integer offerSpeed = extractIntPattern(SPEED_MHZ_PATTERN, ctx.modelLower, 1);

        return findBestMatch(ctx, offerWords, items,
            obj -> (Memory) obj,
            mem -> mem.getComponent(),
            (context, mem) -> {
                Component comp = mem.getComponent();
                String compModelLower = comp.getModel().toLowerCase();
                String[] compWords = compModelLower.split("\\s+");

                if (offerCapacity != null && mem.getCapacity() != null) {
                    if (!offerCapacity.equals(mem.getCapacity())) {
                        return new MatchScore(0.0, "");
                    }
                }

                if (offerType != null && mem.getType() != null) {
                    if (!offerType.equalsIgnoreCase(mem.getType())) {
                        return new MatchScore(0.0, "");
                    }
                }

                double wordsScore = calculateCommonWordsScore(offerWords, compWords, WEIGHT_COMMON_WORDS);
                double modelSim = similarity.apply(context.modelLower, compModelLower);
                double capacityScore = compareInteger(offerCapacity, mem.getCapacity(), WEIGHT_CAPACITY);
                double typeScore = compareString(offerType, mem.getType(), WEIGHT_DDR_TYPE);
                double speedScore = compareInteger(offerSpeed, mem.getSpeed(), WEIGHT_SPEED);
                double total = wordsScore + modelSim + capacityScore + typeScore + speedScore;
                
                return new MatchScore(total, ", capacity=" + offerCapacity + "GB, type=" + offerType + ", speed=" + offerSpeed + "MHz");
            },
            "RAM"
        );
    }

    private Optional<Component> matchMotherboard(OfferContext ctx, String[] offerWords, List<?> items) {
        String offerChipset = extractMotherboardChipset(ctx.modelLower);
        String offerFormat = extractPattern(FORMAT_PATTERN, ctx.modelLower, 0);
        String offerRamType = extractPattern(DDR_TYPE_PATTERN, ctx.modelLower, 1);
        Integer offerRamCapacity = extractIntPattern(CAPACITY_GB_PATTERN, ctx.modelLower, 1);
        String offerSocket = extractPattern(SOCKET_PATTERN, ctx.modelLower, 1);

        return findBestMatch(ctx, offerWords, items,
            obj -> (Motherboard) obj,
            mb -> mb.getComponent(),
            (context, mb) -> {
                Component comp = mb.getComponent();
                String compModelLower = comp.getModel().toLowerCase();
                String[] compWords = compModelLower.split("\\s+");

                if (offerChipset != null && mb.getChipset() != null) {
                    String compChipset = mb.getChipset().toLowerCase();
                    if (!offerChipset.equalsIgnoreCase(compChipset)) {
                        return new MatchScore(0.0, "");
                    }
                }

                double wordsScore = calculateCommonWordsScore(offerWords, compWords, WEIGHT_COMMON_WORDS);
                double modelSim = similarity.apply(context.modelLower, compModelLower);
                double chipsetScore = compareString(offerChipset, mb.getChipset(), WEIGHT_CHIPSET);
                double formatScore = compareString(offerFormat, mb.getFormat(), WEIGHT_FORMAT);
                double ramTypeScore = compareString(offerRamType, mb.getMemoryType(), WEIGHT_RAM_TYPE);
                double ramCapacityScore = compareInteger(offerRamCapacity, mb.getRamCapacity(), WEIGHT_RAM_CAPACITY);
                double socketScore = compareString(offerSocket, mb.getSocketType(), WEIGHT_SOCKET);
                double total = wordsScore + modelSim + chipsetScore + formatScore + ramTypeScore + ramCapacityScore + socketScore;
                
                return new MatchScore(total, ", chipset=" + offerChipset + ", socket=" + offerSocket);
            },
            "MB"
        );
    }

    private Optional<Component> matchPowerSupply(OfferContext ctx, String[] offerWords, List<?> items) {
        Integer offerMaxPower = extractIntPattern(POWER_WATT_PATTERN, ctx.modelLower, 1);

        return findBestMatch(ctx, offerWords, items,
            obj -> (PowerSupply) obj,
            ps -> ps.getComponent(),
            (context, ps) -> {
                Component comp = ps.getComponent();
                String compModelLower = comp.getModel().toLowerCase();
                String[] compWords = compModelLower.split("\\s+");

                if (offerMaxPower != null && ps.getMaxPowerWatt() != null) {
                    if (!offerMaxPower.equals(ps.getMaxPowerWatt())) {
                        return new MatchScore(0.0, "");
                    }
                }

                double wordsScore = calculateCommonWordsScore(offerWords, compWords, WEIGHT_COMMON_WORDS);
                double modelSim = similarity.apply(context.modelLower, compModelLower);
                double powerScore = compareInteger(offerMaxPower, ps.getMaxPowerWatt(), WEIGHT_POWER);
                double total = wordsScore + modelSim + powerScore;
                
                return new MatchScore(total, ", power=" + offerMaxPower + "W");
            },
            "PSU"
        );
    }

        private Optional<Component> matchStorage(OfferContext ctx, String[] offerWords, List<?> items) {
        Double offerCapacityGB = extractStorageCapacityInGB(ctx.modelLower);
    
        return findBestMatch(ctx, offerWords, items,
            obj -> (Storage) obj,
            st -> st.getComponent(),
            (context, st) -> {
                Component comp = st.getComponent();
                String compModelLower = comp.getModel().toLowerCase();
                String[] compWords = compModelLower.split("\\s+");
    
                if (offerCapacityGB != null && st.getCapacity() != null) {
                    if (!offerCapacityGB.equals(st.getCapacity())) {
                        return new MatchScore(0.0, "");
                    }
                }
    
                double wordsScore = calculateCommonWordsScore(offerWords, compWords, WEIGHT_COMMON_WORDS);
                double modelSim = similarity.apply(context.modelLower, compModelLower);
                double capacityScore = offerCapacityGB != null && st.getCapacity() != null && 
                                       offerCapacityGB.equals(st.getCapacity()) ? WEIGHT_CAPACITY : 0.0;
                
                double total = wordsScore + modelSim + capacityScore;
                
                return new MatchScore(total, ", capacity=" + offerCapacityGB + "GB");
            },
            "STORAGE"
        );
    }

    private Optional<Component> matchCase(OfferContext ctx, String[] offerWords, List<?> items) {
        String offerFormat = extractPattern(FORMAT_PATTERN, ctx.modelLower, 0);

        return findBestMatch(ctx, offerWords, items,
            obj -> (Case) obj,
            c -> c.getComponent(),
            (context, c) -> {
                Component comp = c.getComponent();
                String compModelLower = comp.getModel().toLowerCase();
                String[] compWords = compModelLower.split("\\s+");

                double wordsScore = calculateCommonWordsScore(offerWords, compWords, WEIGHT_COMMON_WORDS);
                double modelSim = similarity.apply(context.modelLower, compModelLower);
                double formatScore = compareString(offerFormat, c.getFormat(), WEIGHT_CASE_FORMAT);
                double total = wordsScore + modelSim + formatScore;
                
                return new MatchScore(total, "");
            },
            "CASE"
        );
    }

    private Optional<Component> matchCooler(OfferContext ctx, String[] offerWords, List<?> items) {
        return findBestMatch(ctx, offerWords, items,
            obj -> (Cooler) obj,
            cooler -> cooler.getComponent(),
            (context, cooler) -> {
                Component comp = cooler.getComponent();
                String compModelLower = comp.getModel().toLowerCase();
                String[] compWords = compModelLower.split("\\s+");

                double wordsScore = calculateCommonWordsScore(offerWords, compWords, WEIGHT_COMMON_WORDS);
                double modelSim = similarity.apply(context.modelLower, compModelLower);
                double total = wordsScore + modelSim;
                
                return new MatchScore(total, "");
            },
            "COOLER"
        );
    }



    private String extractGpuChip(String text) {
        if (text == null) return null;
        String lower = text.toLowerCase();

        if (lower.contains("quadro") || lower.contains("rtx a") || 
            lower.contains("tesla") || lower.contains("titan")) {
            return null;
        }

        Pattern[] patterns = {GPU_RTX_PATTERN, GPU_GTX_PATTERN, GPU_GT_PATTERN, GPU_RX_PATTERN};

        for (Pattern p : patterns) {
            Matcher m = p.matcher(lower);
            if (m.find()) {
                return m.group(1).replaceAll("\\s+", " ").trim();
            }
        }
        return null;
    }

    private String extractCpuModel(String text) {
        if (text == null) return null;
        String lower = text.toLowerCase();

        Matcher intelMatcher = CPU_INTEL_PATTERN.matcher(lower);
        if (intelMatcher.find()) {
            String series = intelMatcher.group(1);
            String model = intelMatcher.group(2);
            return (series + "-" + model).toLowerCase();
        }

        Matcher ryzenMatcher = CPU_RYZEN_PATTERN.matcher(lower);
        if (ryzenMatcher.find()) {
            String series = ryzenMatcher.group(1).replaceAll("\\s+", " ");
            String model = ryzenMatcher.group(2);
            return (series + " " + model).toLowerCase();
        }

        return null;
    }

    private String extractMotherboardChipset(String text) {
    if (text == null) return null;
    String lower = text.toLowerCase();
    
   // Intel chipsety: Z790, B760, H610, X299, Z890, W680, Q370, C621, itp.
    Pattern intelPattern = Pattern.compile("\\b([zhbxwqc]\\d{3,4})\\b", Pattern.CASE_INSENSITIVE);
    Matcher intelMatcher = intelPattern.matcher(lower);
    if (intelMatcher.find()) {
        return intelMatcher.group(1).toUpperCase();
    }
    
    // AMD chipsety: B550, X570, A520, X670E, B650, TRX40, WRX80, X870I, itp.
    Pattern amdPattern = Pattern.compile("\\b([abxw]\\d{3,4}[a-z]?|trx\\d{2}|wrx\\d{2})\\b", Pattern.CASE_INSENSITIVE);
    Matcher amdMatcher = amdPattern.matcher(lower);
    if (amdMatcher.find()) {
        return amdMatcher.group(1).toUpperCase();
    }
    
    return null;
}

    private Double extractStorageCapacityInGB(String text) {
        if (text == null) return null;
        
        Matcher matcher = STORAGE_CAPACITY_PATTERN.matcher(text);
        
        if (matcher.find()) {
            try {
                double value = Double.parseDouble(matcher.group(1));
                String unit = matcher.group(2).toLowerCase();
                return "tb".equals(unit) ? value * 1000.0 : value;
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private Double extractClockSpeed(String text) {
        if (text == null) return null;
        
        Matcher ghzMatcher = CLOCK_GHZ_PATTERN.matcher(text);
        if (ghzMatcher.find()) {
            try {
                String value = ghzMatcher.group(1).replace(",", ".");
                return Double.parseDouble(value);
            } catch (NumberFormatException ignored) {
            }
        }
        
        Matcher mhzMatcher = CLOCK_MHZ_PATTERN.matcher(text);
        if (mhzMatcher.find()) {
            try {
                double mhz = Double.parseDouble(mhzMatcher.group(1));
                return mhz / 1000.0;
            } catch (NumberFormatException ignored) {
            }
        }
        
        return null;
    }


    private String normalizeBrandName(String brand) {
        if (brand == null) return null;
        String norm = brand
                .toLowerCase()
                .replaceAll("[^a-z0-9]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        return norm.isEmpty() ? null : norm;
    }

    private boolean isBrandInOfferTitle(Brand componentBrand, String offerTitle) {
        if (componentBrand == null || offerTitle == null) {
            return true;
        }
        String brandNorm = normalizeBrandName(componentBrand.getName());
        String titleNorm = normalizeBrandName(offerTitle);
        if (brandNorm == null || titleNorm == null) {
            return true;
        }
        return titleNorm.contains(brandNorm);
    }

    private double calculateCommonWordsScore(String[] words1, String[] words2, double weight) {
        Set<String> set1 = new HashSet<>(Arrays.asList(words1));
        Set<String> set2 = new HashSet<>(Arrays.asList(words2));
        set1.retainAll(set2);
        return set1.size() * weight;
    }

    private double compareString(String s1, String s2, double weight) {
        return (s1 != null && s2 != null && s1.equalsIgnoreCase(s2)) ? weight : 0.0;
    }

    private double compareInteger(Integer i1, Integer i2, double weight) {
        return (i1 != null && i2 != null && i1.equals(i2)) ? weight : 0.0;
    }

    private String extractPattern(Pattern pattern, String text, int group) {
        if (text == null) return null;
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(group) : null;
    }

    private Integer extractIntPattern(Pattern pattern, String text, int group) {
        if (text == null) return null;
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            try {
                return Integer.valueOf(matcher.group(group));
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private static class MatchScore {
        final double totalScore;
        final String details;

        MatchScore(double totalScore, String details) {
            this.totalScore = totalScore;
            this.details = details;
        }
    }
}