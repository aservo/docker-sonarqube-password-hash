/*
 * SonarQube
 * Copyright (C) 2009-2022 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package de.aservo.sonar;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import static java.lang.String.format;

public class SonarqubePasswordHash {

    // The following values must match the constants from CredentialsLocalAuthentication.java
    private static final int ITERATIONS = 100_000;
    private static final char ITERATIONS_HASH_SEPARATOR = '$';
    private static final int KEY_LEN = 512;
    private static final String ALGORITHM = String.format("PBKDF2WithHmacSHA%d", KEY_LEN);

    public static void main(final String[] args) {
        try {
            assert args.length == 2;

            final String salt = args[0];
            final String password = args[1];

            System.out.println(hashPassword(salt, password));
            System.exit(0);
        } catch (Exception e) {
            System.out.println("usage: sonarqube-password-hash <SALT> <PASSWORD>");
            System.exit(1);
        }
    }

    private static String hashPassword(final String salt, final String password) {
        final byte[] saltBytes = Base64.getDecoder().decode(salt);
        return composeEncryptedPassword(hash(saltBytes, password));
    }

    private static String composeEncryptedPassword(final String hash) {
        return format("%d%c%s", ITERATIONS, ITERATIONS_HASH_SEPARATOR, hash);
    }

    private static String hash(final byte[] salt, final String password) {
        try {
            final SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            final PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LEN);
            final byte[] hash = skf.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }
}
