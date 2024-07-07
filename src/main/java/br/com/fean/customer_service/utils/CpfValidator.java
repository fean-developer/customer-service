package br.com.fean.customer_service.utils;

import java.util.Arrays;

public class CpfValidator {

    private static final int CPF_LENGTH = 11;
    private static final String[] INVALID_CPFS = {
            "00000000000", "11111111111", "22222222222", "33333333333",
            "44444444444", "55555555555", "66666666666", "77777777777",
            "88888888888", "99999999999"
    };

    public static boolean isValid(String cpf) {
        if (isInvalidFormat(cpf)) {
            return false;
        }

        try {
            return hasValidCheckDigits(cpf);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isInvalidFormat(String cpf) {
        return cpf.length() != CPF_LENGTH || isInvalidSequence(cpf);
    }

    private static boolean isInvalidSequence(String cpf) {
        return Arrays.asList(INVALID_CPFS).contains(cpf);
    }

    private static boolean hasValidCheckDigits(String cpf) {
        int firstCheckDigit = calculateCheckDigit(cpf, 10, 9);
        int secondCheckDigit = calculateCheckDigit(cpf, 11, 10);

        return firstCheckDigit == Character.getNumericValue(cpf.charAt(9)) &&
                secondCheckDigit == Character.getNumericValue(cpf.charAt(10));
    }

    private static int calculateCheckDigit(String cpf, int initialWeight, int length) {
        int sum = 0;

        for (int i = 0; i < length; i++) {
            int digit = Character.getNumericValue(cpf.charAt(i));
            sum += digit * initialWeight--;
        }

        int remainder = 11 - (sum % 11);
        return (remainder == 10 || remainder == 11) ? 0 : remainder;
    }

    public static String formatCpf(String cpf) {
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." +
                cpf.substring(6, 9) + "-" + cpf.substring(9, 11);
    }
}