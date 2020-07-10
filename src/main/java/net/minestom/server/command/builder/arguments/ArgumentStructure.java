package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.builder.structure.Structure;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ArgumentStructure extends Argument<Structure> {

    public static final int BRACKET_ERROR = 1;
    public static final int UNKNOWN_MEMBER_ERROR = 2;
    public static final int UNDEFINED_MEMBER_ERROR = 3;
    public static final int INVALID_MEMBER_ERROR = 4;

    private Map<String, Argument> argByKey;
    private Map<String, MemberType> memberTypeByKey;

    private Map<String, Object> optionalDefaultValues;

    public ArgumentStructure(String id) {
        super(id, true);

        this.argByKey = new HashMap<>();
        this.memberTypeByKey = new HashMap<>();

        this.optionalDefaultValues = new HashMap<>();
    }

    public void addNecessaryMember(Argument argument) {
        if (argument.useRemaining()) {
            System.err.println("Array type is unsupported with structure");
            return;
        }

        String key = argument.getId().toLowerCase();
        this.argByKey.put(key, argument);
        this.memberTypeByKey.put(key, MemberType.NECESSARY);
    }

    public void addOptionalMember(Argument argument, Object defaultValue) {
        if (argument.useRemaining()) {
            System.err.println("Array type is unsupported with structure");
            return;
        }

        String key = argument.getId().toLowerCase();
        this.argByKey.put(key, argument);
        this.memberTypeByKey.put(key, MemberType.OPTIONAL);
        this.optionalDefaultValues.put(key, defaultValue);
    }


    @Override
    public int getCorrectionResult(String value) {
        // Check if value start and end with bracket
        char first = value.charAt(0);
        char last = value.charAt(value.length() - 1);
        boolean bracket = first == '[' && last == ']';
        if (!bracket)
            return BRACKET_ERROR;
        // Remove them
        value = value.substring(1, value.length() - 1);

        // Start analyze

        // Get all necessary keys
        List<String> necessaryMembers = memberTypeByKey.keySet().stream().filter(id -> memberTypeByKey.get(id).equals(MemberType.NECESSARY)).collect(Collectors.toList());

        int index = 0;

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);

            if (c == '=') {

                if (index > i)
                    continue;

                String varName = value.substring(index, i).trim().toLowerCase();
                //System.out.println("VARNAME: " + varName);
                int declarationStartIndex = i + 1;

                Argument arg = argByKey.getOrDefault(varName, null);
                if (arg == null) {
                    // There isn't any member with varName name
                    return UNKNOWN_MEMBER_ERROR;
                }

                int[] semicolons = getSemiColonsIndex(value, declarationStartIndex);


                String argValue;
                // There isn't any other initialization
                if (semicolons[0] == -1) {
                    argValue = value.substring(i + 1).trim();
                    if (!isValueValid(arg, argValue)) {
                        // No valid arg
                        return INVALID_MEMBER_ERROR;
                    }
                    necessaryMembers.remove(varName);
                    // END
                    break;
                } else {
                    boolean correct = false;
                    boolean shouldBreak = false;
                    for (int semicolonIndex : semicolons) {
                        if (semicolonIndex == -1) {
                            argValue = value.substring(i + 1).trim();
                            if (!isValueValid(arg, argValue)) {
                                // No valid arg
                                return INVALID_MEMBER_ERROR;
                            }
                            shouldBreak = true;
                            correct = true;
                            necessaryMembers.remove(varName);
                            break;
                        }

                        argValue = value.substring(i + 1, semicolonIndex).trim();
                        if (isValueValid(arg, argValue)) {
                            index = semicolonIndex + 1;
                            correct = true;
                            necessaryMembers.remove(varName);
                            break;
                        }
                    }

                    if (!correct) {
                        return INVALID_MEMBER_ERROR;
                    }

                    if (shouldBreak) {
                        break;
                    }
                }
            }

        }

        return necessaryMembers.isEmpty() ? SUCCESS : UNDEFINED_MEMBER_ERROR;
    }

    @Override
    public Structure parse(String value) {

        Structure struct = new Structure();


        // Remove first and last characters (bracket)
        value = value.substring(1, value.length() - 1);

        List<String> optionalMembers = memberTypeByKey.keySet().stream().filter(id -> memberTypeByKey.get(id).equals(MemberType.OPTIONAL)).collect(Collectors.toList());
        int index = 0;

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);

            if (c == '=') {

                if (index > i)
                    continue;

                String varName = value.substring(index, i).trim().toLowerCase();
                int declarationStartIndex = i + 1;

                Argument arg = argByKey.getOrDefault(varName, null);

                int[] semicolons = getSemiColonsIndex(value, declarationStartIndex);

                String argValue;
                // There isn't any other initialization
                if (semicolons[0] == -1) {
                    argValue = value.substring(i + 1).trim();
                    struct.setValue(varName, arg.parse(argValue));
                    optionalMembers.remove(varName);
                    // END
                    break;
                } else {
                    boolean shouldBreak = false;
                    for (int semicolonIndex : semicolons) {
                        if (semicolonIndex == -1) {
                            argValue = value.substring(i + 1).trim();
                            if (isValueValid(arg, argValue)) {
                                shouldBreak = true;
                                struct.setValue(varName, arg.parse(argValue));
                                optionalMembers.remove(varName);
                            }
                            break;
                        }

                        argValue = value.substring(i + 1, semicolonIndex).trim();
                        if (isValueValid(arg, argValue)) {
                            index = semicolonIndex + 1;
                            struct.setValue(varName, arg.parse(argValue));
                            optionalMembers.remove(varName);
                            break;
                        }
                    }

                    if (shouldBreak) {
                        break;
                    }
                }

                // Check where is next "valid" semicolon (after definition)
                // Take value between member initialization as arg and check validity
                // continue
            }

        }

        if (!optionalMembers.isEmpty()) {
            for (String id : optionalMembers) {
                struct.setValue(id, optionalDefaultValues.get(id));
            }
        }

        return struct;
    }

    @Override
    public int getConditionResult(Structure value) {
        return SUCCESS;
    }

    private boolean isValueValid(Argument argument, String argValue) {
        return argument.getCorrectionResult(argValue) == SUCCESS &&
                argument.getConditionResult(argValue) == SUCCESS;
    }

    private int[] getSemiColonsIndex(String value, int index) {
        int[] array = new int[value.length() - index];

        // Used to see if array has been modified
        Arrays.fill(array, -1);

        int count = 0;
        for (int i = index; i < value.length(); i++) {
            if (value.charAt(i) == ';') {
                array[count] = i;
                count++;
            }
        }
        return array;
    }

    private enum MemberType {
        NECESSARY, OPTIONAL
    }
}
