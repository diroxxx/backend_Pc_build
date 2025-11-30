package org.example.backend_pcbuild.Offer.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.example.backend_pcbuild.Admin.dto.ComponentOfferDto;
import org.example.backend_pcbuild.models.*;
import org.example.backend_pcbuild.repository.BrandRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Service
public class OfferMatchingService {


    private static final JaroWinklerSimilarity similarity = new JaroWinklerSimilarity();


    public Component matchOfferToComponent(
            ComponentType category,
            ComponentOfferDto offerDto,
            List<?> components
    ) {
        if (components == null || components.isEmpty()) {
            System.out.println("[matchOfferToComponent] No items in DB for category: " + category);
            return null;
        }
        if (offerDto == null || offerDto.getTitle() == null) {
            System.out.println("[matchOfferToComponent] Offer has no title, skipping");
            return null;
        }

        String modelOrTitle = offerDto.getModel() != null ? offerDto.getModel() : offerDto.getTitle();
        String offerText = modelOrTitle.toLowerCase();
        String[] offerWords = offerText.split("\\s+");

        return switch (category) {
            case PROCESSOR     -> matchProcessor(offerDto, offerWords, components);
            case GRAPHICS_CARD -> matchGraphicsCard(offerDto, offerWords, components);
            case MEMORY        -> matchMemory(offerDto, offerWords, components);
            case MOTHERBOARD   -> matchMotherboard(offerDto, offerWords, components);
            case POWER_SUPPLY  -> matchPowerSupply(offerDto, offerWords, components);
            case STORAGE       -> matchStorage(offerDto, offerWords, components);
            case CASE_PC       -> matchCase(offerDto, offerWords, components);
            case CPU_COOLER    -> matchCooler(offerDto, offerWords, components);
        };
    }

    private Component matchProcessor(ComponentOfferDto offerDto, String[] offerWords, List<?> items) {
        String offerBrand = offerDto.getBrand();
        String offerTitle = offerDto.getTitle();
        String offerModelRaw = offerDto.getModel() != null ? offerDto.getModel() : offerTitle;
        String offerModelLower = offerModelRaw != null ? offerModelRaw.toLowerCase() : "";
        String offerCpuClass = extractCpuModel(offerModelLower);

        Pattern coresPattern = Pattern.compile("(\\d+)\\s*(rdzeni|rdzenie|cores)", Pattern.CASE_INSENSITIVE);
        Pattern threadsPattern = Pattern.compile("(\\d+)\\s*(wątk|threads)", Pattern.CASE_INSENSITIVE);
        Pattern clockPattern = Pattern.compile("(\\d{1,2}\\.\\d{1,2})\\s*ghz", Pattern.CASE_INSENSITIVE);

        Integer offerCores = extractIntPattern(coresPattern, offerModelLower, 1);
        Integer offerThreads = extractIntPattern(threadsPattern, offerModelLower, 1);
        Double offerBaseClock = null;
        String clockStr = extractPattern(clockPattern, offerModelLower, 1);
        if (clockStr != null) {
            try {
                offerBaseClock = Double.valueOf(clockStr.replace(",", "."));
            } catch (NumberFormatException ignored) {
            }
        }

        Component bestComponent = null;
        double bestScore = 0.0;

        for (Object obj : items) {
            Processor cpu = (Processor) obj;
            Component comp = cpu.getComponent();
            if (comp == null || comp.getModel() == null) continue;

            if (!isBrandInOfferTitle(comp.getBrand(), offerTitle)) {
                continue;
            }

            String compModelLower = comp.getModel().toLowerCase();
            String[] compWords = compModelLower.split("\\s+");
            String compCpuClass = extractCpuModel(compModelLower);

            if (offerCpuClass != null && compCpuClass != null && !offerCpuClass.equals(compCpuClass)) {
                continue;
            }

            double wordsScore = calculateCommonWordsScore(offerWords, compWords, 0.2);
            double modelSim = similarity.apply(offerModelLower, compModelLower);

            double coresScore = 0.0;
            if (offerCores != null && cpu.getCores() != null) {
                coresScore = compareInteger(offerCores, cpu.getCores(), 0.3);
            }

            double threadsScore = 0.0;
            if (offerThreads != null && cpu.getThreads() != null) {
                threadsScore = compareInteger(offerThreads, cpu.getThreads(), 0.2);
            }

            double clockScore = 0.0;
            if (offerBaseClock != null && cpu.getBaseClock() != null) {
                double diff = Math.abs(offerBaseClock - cpu.getBaseClock());
                if (diff < 0.2) {
                    clockScore = 0.4;
                } else if (diff < 0.5) {
                    clockScore = 0.2;
                }
            }

            double classScore = 0.0;
            if (offerCpuClass != null && offerCpuClass.equals(compCpuClass)) {
                classScore = 1.0;
            }

            double score = wordsScore + modelSim  + coresScore + threadsScore + clockScore + classScore;

            if (score > bestScore) {
                bestScore = score;
                bestComponent = comp;
            }
        }

        if (bestComponent != null) {
            System.out.println("[CPU match] Best match for [" + offerBrand + " " + offerModelRaw +
                    "] => " + bestComponent.getBrand().getName() + " " + bestComponent.getModel() +
                    " | score=" + bestScore + ", class=" + offerCpuClass);
        } else {
            System.out.println("[CPU match] No match for [" + offerBrand + " " + offerModelRaw +
                    "], class=" + offerCpuClass);
        }

        return bestComponent;
    }

    private Component matchGraphicsCard(ComponentOfferDto offerDto, String[] offerWords, List<?> items) {
        String offerBrand = offerDto.getBrand();
        String offerTitle = offerDto.getTitle();
        String offerModelRaw = offerDto.getModel() != null ? offerDto.getModel() : offerTitle;
        String offerModelLower = offerModelRaw != null ? offerModelRaw.toLowerCase() : "";
        String offerChip = extractGpuChip(offerModelLower);

        Pattern vramPattern = Pattern.compile("(\\d+)\\s*gb", Pattern.CASE_INSENSITIVE);
        Integer offerVramGb = extractIntPattern(vramPattern, offerModelLower, 1);

        Component bestComponent = null;
        double bestScore = 0.0;

        for (Object obj : items) {
            GraphicsCard gpu = (GraphicsCard) obj;
            Component comp = gpu.getComponent();
            if (comp == null || comp.getModel() == null) continue;

            if (!isBrandInOfferTitle(comp.getBrand(), offerTitle)) {
                continue;
            }

            String compModelLower = comp.getModel().toLowerCase();
            String[] compWords = compModelLower.split("\\s+");
            String compChip = extractGpuChip(compModelLower);

            // jeśli chip wyciągnięty po obu stronach - wymagaj zgodności
            if (offerChip != null && compChip != null && !offerChip.equals(compChip)) {
                continue;
            }

            double wordsScore = calculateCommonWordsScore(offerWords, compWords, 0.3);
            double modelSim = similarity.apply(offerModelLower, compModelLower);

            double vramScore = 0.0;
            if (offerVramGb != null && gpu.getVram() != null) {
                vramScore = compareInteger(offerVramGb, gpu.getVram(), 0.5);
            }

            double chipScore = 0.0;
            if (offerChip != null && compChip != null && offerChip.equals(compChip)) {
                chipScore = 1.0;
            }

            double score = wordsScore + modelSim + vramScore + chipScore;

            if (score > bestScore) {
                bestScore = score;
                bestComponent = comp;
            }
        }

        if (bestComponent != null) {
            System.out.println("[GPU match] Best match for [" + offerBrand + " " + offerModelRaw +
                    "] => " + bestComponent.getBrand().getName() + " " + bestComponent.getModel() +
                    " | score=" + bestScore + ", chip=" + offerChip);
        } else {
            System.out.println("[GPU match] No match for [" + offerBrand + " " + offerModelRaw +
                    "], chip=" + offerChip);
        }

        return bestComponent;
    }

    // ===== MEMORY (RAM) =====
    private Component matchMemory(ComponentOfferDto offerDto, String[] offerWords, List<?> items) {
        String offerBrand = offerDto.getBrand();
        String offerTitle = offerDto.getTitle();
        String offerModelRaw = offerDto.getModel() != null ? offerDto.getModel() : offerTitle;
        String offerModelLower = offerModelRaw != null ? offerModelRaw.toLowerCase() : "";

        Pattern capacityPattern = Pattern.compile("(\\d+)\\s*gb", Pattern.CASE_INSENSITIVE);
        Pattern typePattern = Pattern.compile("(ddr\\d)", Pattern.CASE_INSENSITIVE);
        Pattern speedPattern = Pattern.compile("(\\d{3,5})\\s*mhz", Pattern.CASE_INSENSITIVE);

        Integer offerCapacity = extractIntPattern(capacityPattern, offerModelLower, 1);
        String offerType = extractPattern(typePattern, offerModelLower, 1);
        Integer offerSpeed = extractIntPattern(speedPattern, offerModelLower, 1);

        Component bestComponent = null;
        double bestScore = 0.0;

        for (Object obj : items) {
            Memory mem = (Memory) obj;
            Component comp = mem.getComponent();
            if (comp == null || comp.getModel() == null) continue;
            if (!isBrandInOfferTitle(comp.getBrand(), offerTitle)) {
                continue;
            }

            String compModelLower = comp.getModel().toLowerCase();
            String[] compWords = compModelLower.split("\\s+");

            double wordsScore = calculateCommonWordsScore(offerWords, compWords, 0.2);
            double modelSim = similarity.apply(offerModelLower, compModelLower);

            double capacityScore = 0.0;
            if (offerCapacity != null && mem.getCapacity() != null) {
                capacityScore = compareInteger(offerCapacity, mem.getCapacity(), 0.4);
            }

            double typeScore = 0.0;
            if (offerType != null && mem.getType() != null) {
                typeScore = compareString(offerType, mem.getType(), 0.3);
            }

            double speedScore = 0.0;
            if (offerSpeed != null && mem.getSpeed() != null) {
                speedScore = compareInteger(offerSpeed, mem.getSpeed(), 0.3);
            }

            double score = wordsScore + modelSim + capacityScore + typeScore + speedScore;

            if (score > bestScore) {
                bestScore = score;
                bestComponent = comp;
            }
        }

        if (bestComponent != null) {
            System.out.println("[RAM match] Best match for [" + offerBrand + " " + offerModelRaw +
                    "] => " + bestComponent.getBrand().getName() + " " + bestComponent.getModel() +
                    " | score=" + bestScore);
        } else {
            System.out.println("[RAM match] No match for [" + offerBrand + " " + offerModelRaw + "]");
        }

        return bestComponent;
    }

    // ===== MOTHERBOARD =====
    private Component matchMotherboard(ComponentOfferDto offerDto, String[] offerWords, List<?> items) {
        String offerBrand = offerDto.getBrand();
        String offerTitle = offerDto.getTitle();
        String offerModelRaw = offerDto.getModel() != null ? offerDto.getModel() : offerTitle;
        String offerModelLower = offerModelRaw != null ? offerModelRaw.toLowerCase() : "";

        Pattern chipsetPattern = Pattern.compile("\\w+\\d+", Pattern.CASE_INSENSITIVE);
        Pattern formatPattern = Pattern.compile("\\S*atx|\\S*itx", Pattern.CASE_INSENSITIVE);
        Pattern typePattern = Pattern.compile("(ddr\\d)", Pattern.CASE_INSENSITIVE);
        Pattern maxRamCapacityPattern = Pattern.compile("(\\d+)gb", Pattern.CASE_INSENSITIVE);
        Pattern socketPattern = Pattern.compile("(am4|am5|lga\\d{4}|s\\d{3,4}|fm2|fm1)", Pattern.CASE_INSENSITIVE);

        String offerChipset = extractPattern(chipsetPattern, offerModelLower, 0);
        String offerFormat = extractPattern(formatPattern, offerModelLower, 0);
        String offerRamType = extractPattern(typePattern, offerModelLower, 1);
        Integer offerRamCapacity = extractIntPattern(maxRamCapacityPattern, offerModelLower, 1);
        String offerSocket = extractPattern(socketPattern, offerModelLower, 1);

        Component bestComponent = null;
        double bestScore = 0.0;

        for (Object obj : items) {
            Motherboard mb = (Motherboard) obj;
            Component comp = mb.getComponent();
            if (comp == null || comp.getModel() == null) continue;
            if (!isBrandInOfferTitle(comp.getBrand(), offerTitle)) {
                continue;
            }

            String compModelLower = comp.getModel().toLowerCase();
            String[] compWords = compModelLower.split("\\s+");

            double wordsScore = calculateCommonWordsScore(offerWords, compWords, 0.2);
            double modelSim = similarity.apply(offerModelLower, compModelLower);

            double chipsetScore = compareString(offerChipset, mb.getChipset(), 0.3);
            double formatScore = compareString(offerFormat, mb.getFormat(), 0.2);
            double ramTypeScore = compareString(offerRamType, mb.getMemoryType(), 0.2);
            double ramCapacityScore = compareInteger(offerRamCapacity, mb.getRamCapacity(), 0.2);
            double socketScore = compareString(offerSocket, mb.getSocketType(), 0.4);

            double score = wordsScore + modelSim + chipsetScore + formatScore + ramTypeScore + ramCapacityScore + socketScore;

            if (score > bestScore) {
                bestScore = score;
                bestComponent = comp;
            }
        }

        if (bestComponent != null) {
            System.out.println("[MB match] Best match for [" + offerBrand + " " + offerModelRaw +
                    "] => " + bestComponent.getBrand().getName() + " " + bestComponent.getModel() +
                    " | score=" + bestScore);
        } else {
            System.out.println("[MB match] No match for [" + offerBrand + " " + offerModelRaw + "]");
        }

        return bestComponent;
    }

    // ===== POWER SUPPLY =====
    private Component matchPowerSupply(ComponentOfferDto offerDto, String[] offerWords, List<?> items) {
        String offerBrand = offerDto.getBrand();
        String offerTitle = offerDto.getTitle();
        String offerModelRaw = offerDto.getModel() != null ? offerDto.getModel() : offerTitle;
        String offerModelLower = offerModelRaw != null ? offerModelRaw.toLowerCase() : "";

        Pattern maxPowerPattern = Pattern.compile("(\\d+)\\s*w", Pattern.CASE_INSENSITIVE);
        Integer offerMaxPower = extractIntPattern(maxPowerPattern, offerModelLower, 1);

        Component bestComponent = null;
        double bestScore = 0.0;

        for (Object obj : items) {
            PowerSupply ps = (PowerSupply) obj;
            Component comp = ps.getComponent();
            if (comp == null || comp.getModel() == null) continue;
            if (!isBrandInOfferTitle(comp.getBrand(), offerTitle)) {
                continue;
            }

            String compModelLower = comp.getModel().toLowerCase();
            String[] compWords = compModelLower.split("\\s+");

            double wordsScore = calculateCommonWordsScore(offerWords, compWords, 0.2);
            double modelSim = similarity.apply(offerModelLower, compModelLower);

            double powerScore = 0.0;
            if (offerMaxPower != null && ps.getMaxPowerWatt() != null) {
                powerScore = compareInteger(offerMaxPower, ps.getMaxPowerWatt(), 0.4);
            }

            double score = wordsScore + modelSim + powerScore;

            if (score > bestScore) {
                bestScore = score;
                bestComponent = comp;
            }
        }

        if (bestComponent != null) {
            System.out.println("[PSU match] Best match for [" + offerBrand + " " + offerModelRaw +
                    "] => " + bestComponent.getBrand().getName() + " " + bestComponent.getModel() +
                    " | score=" + bestScore);
        } else {
            System.out.println("[PSU match] No match for [" + offerBrand + " " + offerModelRaw + "]");
        }

        return bestComponent;
    }

    // ===== STORAGE =====
    private Component matchStorage(ComponentOfferDto offerDto, String[] offerWords, List<?> items) {
        String offerBrand = offerDto.getBrand();
        String offerTitle = offerDto.getTitle();
        String offerModelRaw = offerDto.getModel() != null ? offerDto.getModel() : offerTitle;
        String offerModelLower = offerModelRaw != null ? offerModelRaw.toLowerCase() : "";

        Pattern capacityPattern = Pattern.compile("(\\d+)\\s*(gb|tb)", Pattern.CASE_INSENSITIVE);
        Integer offerCapacity = extractIntPattern(capacityPattern, offerModelLower, 1);

        Component bestComponent = null;
        double bestScore = 0.0;

        for (Object obj : items) {
            Storage st = (Storage) obj;
            Component comp = st.getComponent();
            if (comp == null || comp.getModel() == null) continue;
            if (!isBrandInOfferTitle(comp.getBrand(), offerTitle)) {
                continue;
            }

            String compModelLower = comp.getModel().toLowerCase();
            String[] compWords = compModelLower.split("\\s+");

            double wordsScore = calculateCommonWordsScore(offerWords, compWords, 0.2);
            double modelSim = similarity.apply(offerModelLower, compModelLower);

            double capacityScore = 0.0;
            if (offerCapacity != null && st.getCapacity() != null) {
                capacityScore = compareDouble(offerCapacity.doubleValue(), st.getCapacity(), 0.4);
            }

            double score = wordsScore + modelSim + capacityScore;

            if (score > bestScore) {
                bestScore = score;
                bestComponent = comp;
            }
        }

        if (bestComponent != null) {
            System.out.println("[STORAGE match] Best match for [" + offerBrand + " " + offerModelRaw +
                    "] => " + bestComponent.getBrand().getName() + " " + bestComponent.getModel() +
                    " | score=" + bestScore);
        } else {
            System.out.println("[STORAGE match] No match for [" + offerBrand + " " + offerModelRaw + "]");
        }

        return bestComponent;
    }

    private Component matchCase(ComponentOfferDto offerDto, String[] offerWords, List<?> items) {
        String offerBrand = offerDto.getBrand();
        String offerTitle = offerDto.getTitle();
        String offerModelRaw = offerDto.getModel() != null ? offerDto.getModel() : offerTitle;
        String offerModelLower = offerModelRaw != null ? offerModelRaw.toLowerCase() : "";

        Pattern formatPattern = Pattern.compile("\\S*atx|\\S*itx", Pattern.CASE_INSENSITIVE);
        String offerFormat = extractPattern(formatPattern, offerModelLower, 0);

        Component bestComponent = null;
        double bestScore = 0.0;

        for (Object obj : items) {
            Case c = (Case) obj;
            Component comp = c.getComponent();
            if (comp == null || comp.getModel() == null) continue;
            if (!isBrandInOfferTitle(comp.getBrand(), offerTitle)) {
                continue;
            }

            String compModelLower = comp.getModel().toLowerCase();
            String[] compWords = compModelLower.split("\\s+");

            double wordsScore = calculateCommonWordsScore(offerWords, compWords, 0.2);
            double modelSim = similarity.apply(offerModelLower, compModelLower);
            double formatScore = compareString(offerFormat, c.getFormat(), 0.3);

            double score = wordsScore + modelSim + formatScore;

            if (score > bestScore) {
                bestScore = score;
                bestComponent = comp;
            }
        }

        if (bestComponent != null) {
            System.out.println("[CASE match] Best match for [" + offerBrand + " " + offerModelRaw +
                    "] => " + bestComponent.getBrand().getName() + " " + bestComponent.getModel() +
                    " | score=" + bestScore);
        } else {
            System.out.println("[CASE match] No match for [" + offerBrand + " " + offerModelRaw + "]");
        }

        return bestComponent;
    }

    private Component matchCooler(ComponentOfferDto offerDto, String[] offerWords, List<?> items) {
        String offerBrand = offerDto.getBrand();
        String offerTitle = offerDto.getTitle();
        String offerModelRaw = offerDto.getModel() != null ? offerDto.getModel() : offerTitle;
        String offerModelLower = offerModelRaw != null ? offerModelRaw.toLowerCase() : "";

        Component bestComponent = null;
        double bestScore = 0.0;

        for (Object obj : items) {
            Cooler cooler = (Cooler) obj;
            Component comp = cooler.getComponent();
            if (comp == null || comp.getModel() == null) continue;
            if (!isBrandInOfferTitle(comp.getBrand(), offerTitle)) {
                continue;
            }

            String compModelLower = comp.getModel().toLowerCase();
            String[] compWords = compModelLower.split("\\s+");

            double wordsScore = calculateCommonWordsScore(offerWords, compWords, 0.2);
            double modelSim = similarity.apply(offerModelLower, compModelLower);

            double score = wordsScore + modelSim;

            if (score > bestScore) {
                bestScore = score;
                bestComponent = comp;
            }
        }

        if (bestComponent != null) {
            System.out.println("[COOLER match] Best match for [" + offerBrand + " " + offerModelRaw +
                    "] => " + bestComponent.getBrand().getName() + " " + bestComponent.getModel() +
                    " | score=" + bestScore);
        } else {
            System.out.println("[COOLER match] No match for [" + offerBrand + " " + offerModelRaw + "]");
        }

        return bestComponent;
    }

    private String extractGpuChip(String text) {
        if (text == null) return null;
        String lower = text.toLowerCase();

        Pattern[] patterns = new Pattern[] {
                Pattern.compile("(rtx\\s*\\d{3,4})"),          // rtx 2060, rtx2060
                Pattern.compile("(gtx\\s*\\d{3,4})"),          // gtx 970
                Pattern.compile("(gt\\s*\\d{3,4})"),           // gt 1030
                Pattern.compile("(rx\\s*\\d{3,4}\\s*xt?)"),    // rx 5700 xt, rx 5700
                Pattern.compile("(rx\\s*\\d{3,4})")            // rx 560
        };

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

        Pattern intelPattern = Pattern.compile("(i[3,5,7,9])[-\\s]?(\\d{3,4}[a-z0-9]*)");
        Matcher intelMatcher = intelPattern.matcher(lower);
        if (intelMatcher.find()) {
            String series = intelMatcher.group(1);
            String model = intelMatcher.group(2);
            return (series + "-" + model).toLowerCase();
        }

        Pattern ryzenPattern = Pattern.compile("(ryzen\\s*[3,5,7,9])\\s*(\\d{3,4}[a-z0-9]*)");
        Matcher ryzenMatcher = ryzenPattern.matcher(lower);
        if (ryzenMatcher.find()) {
            String series = ryzenMatcher.group(1).replaceAll("\\s+", " ");
            String model = ryzenMatcher.group(2);
            return (series + " " + model).toLowerCase();
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


    private boolean isBrandCompatible(String offerBrand, Brand componentBrand, String componentModel, String componentTitleOrModel) {
        String offerBrandNorm = normalizeBrandName(offerBrand);
        if (offerBrandNorm == null) {
            return true;
        }

        String compBrandNorm = componentBrand != null ? normalizeBrandName(componentBrand.getName()) : null;
        String compModelNorm = componentModel != null ? normalizeBrandName(componentModel) : null;
        String compTitleNorm = componentTitleOrModel != null ? normalizeBrandName(componentTitleOrModel) : null;

        // 1) dokładne dopasowanie brandu
        if (compBrandNorm != null && offerBrandNorm.equals(compBrandNorm)) {
            return true;
        }

        // 2) brand oferty zawiera się w modelu / tytule komponentu
        if (compModelNorm != null && compModelNorm.contains(offerBrandNorm)) {
            return true;
        }
        if (compTitleNorm != null && compTitleNorm.contains(offerBrandNorm)) {
            return true;
        }

        // 3) odwrotnie (komponent brand w tytule oferty) – możesz dodać jeśli masz tytuł komponentu
        return false;
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
        int common = 0;
        for (String w1 : words1) {
            for (String w2 : words2) {
                if (w1.equals(w2)) common++;
            }
        }
        return common * weight;
    }

    private double compareString(String s1, String s2, double weight) {
        return (s1 != null && s2 != null && s1.equalsIgnoreCase(s2)) ? weight : 0.0;
    }

    private double compareInteger(Integer i1, Integer i2, double weight) {
        return (i1 != null && i2 != null && i1.equals(i2)) ? weight : 0.0;
    }

    private double compareDouble(Double d1, Double d2, double weight) {
        return (d1 != null && d2 != null && d1.equals(d2)) ? weight : 0.0;
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

}
