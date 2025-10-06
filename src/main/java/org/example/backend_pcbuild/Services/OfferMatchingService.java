package org.example.backend_pcbuild.Services;

import lombok.RequiredArgsConstructor;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.example.backend_pcbuild.models.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Service
public class OfferMatchingService {
    private static final JaroWinklerSimilarity similarity = new JaroWinklerSimilarity();


    public Item matchOfferToItem(String category, Map<String, Object> offerData, List<?> items) {
        String offerModel = ((String) offerData.get("model")).toLowerCase();
        String offerBrand = ((String) offerData.get("brand"));
        String offerLower = offerModel.toLowerCase();
        String[] offerWords = offerModel.split(" ");

        Item bestItem = null;
        double bestScore = 0.0;

        switch (category.toLowerCase()) {
            case "processor":
                Pattern socketPattern = Pattern.compile("(am4|am5|lga\\d{4}|s\\d{3,4}|fm2|fm1)", Pattern.CASE_INSENSITIVE);
                Pattern clockPattern = Pattern.compile("(\\d{1,2}\\.\\d{1,2}\\s?ghz)", Pattern.CASE_INSENSITIVE);
                Pattern coresPattern = Pattern.compile("(\\d+)\\s*(rdzeni|cores)", Pattern.CASE_INSENSITIVE);
                Pattern threadsPattern = Pattern.compile("(\\d+)\\s*(wątk|threads)", Pattern.CASE_INSENSITIVE);

                String offerSocket = extractPattern(socketPattern, offerLower, 1);
                String offerClock = extractPattern(clockPattern, offerLower, 1);
                Integer offerCores = extractIntPattern(coresPattern, offerLower, 1);
                Integer offerThreads = extractIntPattern(threadsPattern, offerLower, 1);

                for (Object obj : items) {
                    Processor processor = (Processor) obj;
                    String[] itemWords = processor.getItem().getModel().toLowerCase().split(" ");
                    double score = calculateCommonWordsScore(offerWords, itemWords, 0.2);

                    score += compareBrand(offerBrand, processor.getItem().getBrand(), 0.3);
                    score += compareString(offerSocket, processor.getSocket_type(), 0.3);
                    score += compareString(offerClock, processor.getBase_clock(), 0.2);
                    score += compareInteger(offerCores, processor.getCores(), 0.2);
                    score += compareInteger(offerThreads, processor.getThreads(), 0.2);

                    if (score > bestScore) {
                        bestScore = score;
                        bestItem = processor.getItem();
                    }
                }
                break;
            case "graphics_card":
                Pattern gddrMemoryPattern = Pattern.compile("(gddr\\d)", Pattern.CASE_INSENSITIVE);
                Pattern vramPattern = Pattern.compile("(\\d+)\\s*gb", Pattern.CASE_INSENSITIVE);

                String matchedGddr = extractPattern(gddrMemoryPattern, offerLower, 1);
                Integer matchedVramGb = extractIntPattern(vramPattern, offerLower, 1);

                for (Object obj : items) {
                    GraphicsCard graphicsCard = (GraphicsCard) obj;
                    String[] itemWords = graphicsCard.getItem().getModel().toLowerCase().split(" ");
                    double score = calculateCommonWordsScore(offerWords, itemWords, 0.2);

                    score += compareBrand(offerBrand, graphicsCard.getItem().getBrand(), 0.3);
                    score += similarity.apply(offerModel, graphicsCard.getItem().getModel());
                    score += compareInteger(matchedVramGb, graphicsCard.getVram(), 0.2);
                    score += compareString(matchedGddr, graphicsCard.getGddr(), 0.2);

                    if (score > bestScore) {
                        bestScore = score;
                        bestItem = graphicsCard.getItem();
                    }
                }
                break;
            case "ram":
                Pattern capacityPattern = Pattern.compile("(\\d+)\\s*gb", Pattern.CASE_INSENSITIVE);
                Pattern speedPattern = Pattern.compile("(\\d{3,5})\\s*mhz", Pattern.CASE_INSENSITIVE);
                Pattern typePattern = Pattern.compile("(ddr\\d)", Pattern.CASE_INSENSITIVE);

                Integer matchedCapacity = extractIntPattern(capacityPattern, offerLower, 1);
                Integer matchedSpeed = extractIntPattern(speedPattern, offerLower, 1);
                String matchedType = extractPattern(typePattern, offerLower, 1);

                for (Object obj : items) {
                    Memory memory = (Memory) obj;
                    String[] itemWords = memory.getItem().getModel().toLowerCase().split(" ");
                    double score = calculateCommonWordsScore(offerWords, itemWords, 0.2);

                    score += compareBrand(offerBrand, memory.getItem().getBrand(), 0.3);
                    score += similarity.apply(offerModel, memory.getItem().getModel());
                    score += compareInteger(matchedCapacity, memory.getCapacity(), 0.2);
                    score += compareString(matchedType, memory.getType(), 0.2);
                    score += memory.getSpeed() != null && matchedSpeed != null && memory.getSpeed().toLowerCase().contains(matchedSpeed.toString()) ? 0.1 : 0.0;

                    if (score > bestScore) {
                        bestScore = score;
                        bestItem = memory.getItem();
                    }
                }
                break;
            case "case":
                Pattern formatPattern = Pattern.compile("\\S*atx|\\S*itx", Pattern.CASE_INSENSITIVE);
                String matchedFormat = extractPattern(formatPattern, offerLower, 0);

                for (Object obj : items) {
                    Case casePc = (Case) obj;
                    double score = 0.0;
                    score += compareBrand(offerBrand, casePc.getItem().getBrand(), 0.3);
                    score += similarity.apply(offerModel, casePc.getItem().getModel());
                    score += compareString(matchedFormat, casePc.getFormat(), 0.2);

                    if (score > bestScore) {
                        bestScore = score;
                        bestItem = casePc.getItem();
                    }
                }
                break;

            case "storage":
                Pattern capacityPatternStorage = Pattern.compile("(\\d+)\\s*(gb|tb)", Pattern.CASE_INSENSITIVE);
                Integer matchedCapacityStorage = extractIntPattern(capacityPatternStorage, offerLower, 1); // 1 - liczba
                String matchedUnit = extractPattern(capacityPatternStorage, offerLower, 2); // 2 - jednostka

                for (Object obj : items) {
                    Storage storage = (Storage) obj;
                    double score = 0.0;
                    score += compareBrand(offerBrand, storage.getItem().getBrand(), 0.3);
                    score += similarity.apply(offerModel, storage.getItem().getModel());
                    // Zakładam, że Storage ma getCapacity() (w GB lub TB) oraz getUnit() ("GB"/"TB")
                    score += compareDouble((double) matchedCapacityStorage, ( storage.getCapacity()), 0.2);
//                    score += compareString(matchedUnit, storage.getUnit(), 0.1);

                    if (score > bestScore) {
                        bestScore = score;
                        bestItem = storage.getItem();
                    }
                }
                break;

            case "power_supply":
                Pattern maxPowerPattern = Pattern.compile("(\\d+)\\s*w", Pattern.CASE_INSENSITIVE);
                Integer matchedMaxPower = extractIntPattern(maxPowerPattern, offerLower, 1);

                for (Object obj : items) {
                    PowerSupply powerSupply = (PowerSupply) obj;
                    double score = 0.0;
                    score += compareBrand(offerBrand, powerSupply.getItem().getBrand(), 0.3);
                    score += similarity.apply(offerModel, powerSupply.getItem().getModel());
                    score += compareInteger(matchedMaxPower, powerSupply.getMaxPowerWatt(), 0.2);

                    if (score > bestScore) {
                        bestScore = score;
                        bestItem = powerSupply.getItem();
                    }
                }
                break;

            case "motherboard":
                Pattern chipsetPattern = Pattern.compile("\\w+\\d+", Pattern.CASE_INSENSITIVE);
                Pattern formatPatternMb = Pattern.compile("\\S*atx|\\S*itx", Pattern.CASE_INSENSITIVE);
                Pattern typePatternMb = Pattern.compile("(ddr\\d)", Pattern.CASE_INSENSITIVE);
                Pattern maxRamCapacityPattern = Pattern.compile("(\\d+)gb", Pattern.CASE_INSENSITIVE);
                Pattern maxRamSlotsPattern = Pattern.compile("(\\d+)", Pattern.CASE_INSENSITIVE);
                Pattern socketPatternMb = Pattern.compile("(am4|am5|lga\\d{4}|s\\d{3,4}|fm2|fm1)", Pattern.CASE_INSENSITIVE);

                String matchedChipset = extractPattern(chipsetPattern, offerLower, 0);
                String matchedFormatMb = extractPattern(formatPatternMb, offerLower, 0);
                String matchedRamTypeMb = extractPattern(typePatternMb, offerLower, 1);
                Integer matchedRamCapacityMb = extractIntPattern(maxRamCapacityPattern, offerLower, 1);
                Integer matchedRamSlotsMb = extractIntPattern(maxRamSlotsPattern, offerLower, 1);
                String matchedSocketMb = extractPattern(socketPatternMb, offerLower, 1);

                for (Object obj : items) {
                    Motherboard motherboard = (Motherboard) obj;
                    double score = 0.0;
                    score += compareBrand(offerBrand, motherboard.getItem().getBrand(), 0.3);
                    score += similarity.apply(offerModel, motherboard.getItem().getModel());
                    score += compareString(matchedChipset, motherboard.getChipset(), 0.2);
                    score += compareString(matchedFormatMb, motherboard.getFormat(), 0.1);
                    score += compareString(matchedRamTypeMb, motherboard.getMemoryType(), 0.1);
                    score += compareInteger(matchedRamCapacityMb, motherboard.getRamCapacity(), 0.1);
                    score += compareInteger(matchedRamSlotsMb, motherboard.getRamSlots(), 0.1);
                    score += compareString(matchedSocketMb, motherboard.getSocketType(), 0.2);

                    if (score > bestScore) {
                        bestScore = score;
                        bestItem = motherboard.getItem();
                    }
                }
                break;

            case "cpu_cooler":
//                Pattern coolerTypePattern = Pattern.compile("(air|water)", Pattern.CASE_INSENSITIVE);
//                String matchedCoolerType = extractPattern(coolerTypePattern, offerLower, 1);

                for (Object obj : items) {
                    Cooler cooler = (Cooler) obj;
                    double score = 0.0;
                    score += compareBrand(offerBrand, cooler.getItem().getBrand(), 0.3);
                    score += similarity.apply(offerModel, cooler.getItem().getModel());
//                    score += compareString(matchedCoolerType, cooler.getType(), 0.2);

                    if (score > bestScore) {
                        bestScore = score;
                        bestItem = cooler.getItem();
                    }
                }
                break;
        }
        return bestItem;
    }

        private double calculateCommonWordsScore(String[] words1, String[] words2, double weight) {
            int common = 0;
            for (String w1 : words1)
                for (String w2 : words2)
                    if (w1.equals(w2)) common++;
            return common * weight;
        }

        private double compareBrand(String offerBrand, String itemBrand, double weight) {
            return (itemBrand != null && offerBrand != null && offerBrand.equalsIgnoreCase(itemBrand)) ? weight : 0.0;
        }

        private double compareString(String s1, String s2, double weight) {
            return (s1 != null && s2 != null && s1.equalsIgnoreCase(s2)) ? weight : 0.0;
        }

        private double compareInteger(Integer i1, Integer i2, double weight) {
            return (i1 != null && i2 != null && i1.equals(i2)) ? weight : 0.0;
        }
    private double compareDouble(Double i1, Double i2, double weight) {
        return (i1 != null && i2 != null && i1.equals(i2)) ? weight : 0.0;
    }

        private String extractPattern(Pattern pattern, String text, int group) {
            Matcher matcher = pattern.matcher(text);
            return matcher.find() ? matcher.group(group) : null;
        }

        private Integer extractIntPattern(Pattern pattern, String text, int group) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                try {
                    return Integer.valueOf(matcher.group(group));
                } catch (NumberFormatException ignored) {}
            }
            return null;
        }

}
