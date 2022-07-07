/*
 * Copyright (C) 2021 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.apushnikov.sommelier_notebook.billing;
/*
 * This class is an sample of how you can check to make sure your purchases on the device came
 * from Google Play. Putting code like this on your server will provide additional protection.
 * <p>
 * One thing that you may also wish to consider doing is caching purchase IDs to make replay
 * attacks harder. The reason this code isn't just part of the library is to allow
 * you to customize it (and rename it!) to make generic patching exploits more difficult.
 * <p>
 * Этот класс представляет собой пример того, как вы можете проверить, были ли ваши покупки на
 * устройстве сделаны из Google Play.
 * Размещение такого кода на вашем сервере обеспечит дополнительную защиту
 * <p>
 * Еще одна вещь, которую вы, возможно, захотите сделать, - это кэширование идентификаторов покупок,
 * чтобы усложнить повторные атаки. Причина, по которой этот код - не просто часть библиотеки,
 * заключается в том, чтобы позволить вам настроить его (и переименовать!),
 * Чтобы усложнить общие эксплойты исправления.
 */

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

//import com.apushnikov.other301_mypurchases.BuildConfig;

import com.apushnikov.sommelier_notebook.BuildConfig;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

/**============================================================================================
 * Методы, связанные с безопасностью.
 * Для безопасной реализации весь этот код должен быть реализован на сервере, который
 * взаимодействует с приложением на устройстве.
 * <p>
 * Security-related methods. For a secure implementation, all of this code should be implemented on
 * a server that communicates with the application on the device.
 */
class Security {
    static final private String TAG = "IABUtil/Security";
    static final private String KEY_FACTORY_ALGORITHM = "RSA";
    static final private String SIGNATURE_ALGORITHM = "SHA1withRSA";

    /**
     * BASE_64_ENCODED_PUBLIC_KEY должен быть ПУБЛИЧНЫМ КЛЮЧОМ ВАШЕГО ПРИЛОЖЕНИЯ.
     * В настоящее время его можно получить в консоли разработчика Google Play в категории
     * «Настройка монетизации» в области лицензирования.
     * Эта сборка настроена таким образом, что если вы определите base64EncodedPublicKey
     * в своем local.properties, она будет отражена в BuildConfig.
     * <p>
     * BASE_64_ENCODED_PUBLIC_KEY should be YOUR APPLICATION PUBLIC KEY. You currently get this
     * from the Google Play developer console under the "Monetization Setup" category in the
     * Licensing area. This build has been setup so that if you define base64EncodedPublicKey in
     * your local.properties, it will be echoed into BuildConfig.
     */
    final private static String BASE_64_ENCODED_PUBLIC_KEY = BuildConfig.BASE64_ENCODED_PUBLIC_KEY;

    /**=====================================================================================
     * Проверяет, что данные были подписаны данной подписью
     * <p>
     * Verifies that the data was signed with the given signature
     *
     * @param signedData подписанная строка JSON (подписанная, не зашифрованная)
     * @param signature  подпись для данных, подписанная закрытым ключом
     * =====================================================================================
     */
    static public boolean verifyPurchase(String signedData, String signature) {
        if ((TextUtils.isEmpty(signedData) || TextUtils.isEmpty(BASE_64_ENCODED_PUBLIC_KEY)
                || TextUtils.isEmpty(signature))
        ) {
            Log.w(TAG, "Ошибка проверки покупки в модуле Security: отсутствуют данные.");
//            Log.w(TAG, "Purchase verification failed: missing data.");
            return false;
        }
        try {
            PublicKey key = generatePublicKey(BASE_64_ENCODED_PUBLIC_KEY);
            return verify(key, signedData, signature);
        } catch (IOException e) {
            Log.e(TAG, "Ошибка создания PublicKey из закодированного ключа: " + e.getMessage());
//            Log.e(TAG, "Error generating PublicKey from encoded key: " + e.getMessage());
            return false;
        }
    }

    /**=====================================================================================
     * Создает экземпляр PublicKey из строки, содержащей открытый ключ в кодировке Base64.
     * <p>
     * Generates a PublicKey instance from a string containing the Base64-encoded public key.
     *
     * @param encodedPublicKey Base64-encoded public key
     * @throws IOException if encoding algorithm is not supported or key specification
     *                     is invalid
     * =====================================================================================
     */
    static private PublicKey generatePublicKey(String encodedPublicKey) throws IOException {
        try {
            byte[] decodedKey = Base64.decode(encodedPublicKey, Base64.DEFAULT);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
            return keyFactory.generatePublic(new X509EncodedKeySpec(decodedKey));
        } catch (NoSuchAlgorithmException e) {
            // "RSA" is guaranteed to be available.
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            String msg = "Invalid key specification: " + e;
            Log.w(TAG, msg);
            throw new IOException(msg);
        }
    }

    /**=====================================================================================
     * Проверяет, соответствует ли подпись с сервера вычисленной подписи данных.
     * Возвращает истину, если данные правильно подписаны.
     * <p>
     * Verifies that the signature from the server matches the computed signature on the data.
     * Returns true if the data is correctly signed.
     *
     * @param publicKey  public key associated with the developer account
     * @param signedData signed data from server
     * @param signature  server signature
     * @return true if the data and signature match
     * =====================================================================================
     */
    static private Boolean verify(PublicKey publicKey, String signedData, String signature) {
        byte[] signatureBytes;
        try {
            signatureBytes = Base64.decode(signature, Base64.DEFAULT);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Base64 decoding failed.");
            return false;
        }
        try {
            Signature signatureAlgorithm = Signature.getInstance(SIGNATURE_ALGORITHM);
            signatureAlgorithm.initVerify(publicKey);
            signatureAlgorithm.update(signedData.getBytes());
            if (!signatureAlgorithm.verify(signatureBytes)) {
                Log.w(TAG, "Signature verification failed...");
                return false;
            }
            return true;
        } catch (NoSuchAlgorithmException e) {
            // "RSA" is guaranteed to be available.
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            Log.e(TAG, "Invalid key specification.");
        } catch (SignatureException e) {
            Log.e(TAG, "Signature exception.");
        }
        return false;
    }
}
