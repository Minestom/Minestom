package net.minestom.server.command.builder.arguments.number;

public class ArgumentFloat extends ArgumentNumber<Float> {

    public ArgumentFloat(String id) {
        super(id);
        this.min = Float.MIN_VALUE;
        this.max = Float.MAX_VALUE;
    }

    @Override
    public int getCorrectionResult(String value) {
        try {
            String parsed = parseValue(value);
            int radix = getRadix(value);
            if (radix != 10) {
                Integer.parseInt(parsed, radix);
            } else {
                Float.parseFloat(parsed);
            }
            return SUCCESS;
        } catch (NumberFormatException | NullPointerException e) {
            return NOT_NUMBER_ERROR;
        }
    }

    @Override
    public Float parse(String value) {
        String parsed = parseValue(value);
        int radix = getRadix(value);
        if (radix != 10) {
            return (float) Integer.parseInt(parsed, radix);
        }
        return Float.parseFloat(parsed);
    }

    @Override
    public int getConditionResult(Float value) {
        // Check range
        if (hasMin && value < min) {
            return RANGE_ERROR;
        }
        if (hasMax && value > max) {
            return RANGE_ERROR;
        }

        return SUCCESS;
    }

}
