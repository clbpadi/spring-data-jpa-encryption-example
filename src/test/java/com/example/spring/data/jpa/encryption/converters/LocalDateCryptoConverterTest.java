package com.example.spring.data.jpa.encryption.converters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.AdditionalAnswers.returnsSecondArg;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
class LocalDateCryptoConverterTest {

    private static final LocalDate LOCAL_DATE_TO_CIPHER = LocalDate.of(2017, 3, 28);
    private static final String LOCAL_DATE_TO_CIPHER_AS_STRING = LOCAL_DATE_TO_CIPHER.format(DateTimeFormatter.ISO_DATE);
    private static final String LOCAL_DATE_TO_DECIPHER_AS_STRING = "MjAxNy0wMy0yOA==";

    private LocalDateCryptoConverter localDateCryptoConverter;

    private LocalDateCryptoConverter spiedLocalDateCryptoConverter;

    @Mock
    private CipherInitializer cipherInitializer;

    @BeforeEach
    void setUp() throws Exception {
        localDateCryptoConverter = new LocalDateCryptoConverter(cipherInitializer);

        spiedLocalDateCryptoConverter = spy(localDateCryptoConverter);
        doAnswer(returnsSecondArg()).when(spiedLocalDateCryptoConverter).callCipherDoFinal(any(), any());

        KeyProperty.DATABASE_ENCRYPTION_KEY = "MySuperSecretKey";
    }

    @Nested
    class ConvertToDatabaseColumnShould {

        @Test
        void return_null_string_when_local_date_to_encrypt_is_null() {
            // Given
            LocalDate attribute = null;

            // When
            String result = localDateCryptoConverter.convertToDatabaseColumn(attribute);

            // Then
            assertThat(result).isNull();
        }

        @Test
        void return_encrypted_string_as_base_64() throws Exception {
            // Given
            Cipher cipher = mock(Cipher.class);
            when(cipherInitializer.prepareAndInitCipher(Cipher.ENCRYPT_MODE, KeyProperty.DATABASE_ENCRYPTION_KEY)).thenReturn(cipher);

            // When
            String result = spiedLocalDateCryptoConverter.convertToDatabaseColumn(LOCAL_DATE_TO_CIPHER);

            // Then
            verify(spiedLocalDateCryptoConverter).callCipherDoFinal(cipher, LOCAL_DATE_TO_CIPHER_AS_STRING.getBytes());
            assertThat(result).isEqualTo(LOCAL_DATE_TO_DECIPHER_AS_STRING);
        }

        @Test
        void return_formatted_local_date_but_not_encrypted_when_database_encryption_key_is_null() {
            // Given
            KeyProperty.DATABASE_ENCRYPTION_KEY = null;

            // When
            String result = spiedLocalDateCryptoConverter.convertToDatabaseColumn(LOCAL_DATE_TO_CIPHER);

            // Then
            assertThat(result).isEqualTo(LOCAL_DATE_TO_CIPHER_AS_STRING);
        }

        @Test
        void return_formatted_local_date_but_not_encrypted_when_database_encryption_key_is_empty() {
            // Given
            KeyProperty.DATABASE_ENCRYPTION_KEY = "";

            // When
            String result = spiedLocalDateCryptoConverter.convertToDatabaseColumn(LOCAL_DATE_TO_CIPHER);

            // Then
            assertThat(result).isEqualTo(LOCAL_DATE_TO_CIPHER_AS_STRING);
        }

        @Test
        void rethrow_exception_when_cipher_initialization_fails_with_InvalidKeyException() throws Exception {
            // Given
            InvalidKeyException invalidKeyException = new InvalidKeyException();
            when(cipherInitializer.prepareAndInitCipher(anyInt(), anyString())).thenThrow(invalidKeyException);

            // When
            Throwable throwable = catchThrowable(() -> spiedLocalDateCryptoConverter.convertToDatabaseColumn(LOCAL_DATE_TO_CIPHER));

            // Then
            assertThat(throwable).isInstanceOf(RuntimeException.class).hasCause(invalidKeyException);
        }

        @Test
        void rethrow_exception_when_cipher_initialization_fails_with_NoSuchAlgorithmException() throws Exception {
            // Given
            NoSuchAlgorithmException noSuchAlgorithmException = new NoSuchAlgorithmException();
            when(cipherInitializer.prepareAndInitCipher(anyInt(), anyString())).thenThrow(noSuchAlgorithmException);

            // When
            Throwable throwable = catchThrowable(() -> spiedLocalDateCryptoConverter.convertToDatabaseColumn(LOCAL_DATE_TO_CIPHER));

            // Then
            assertThat(throwable).isInstanceOf(RuntimeException.class).hasCause(noSuchAlgorithmException);
        }

        @Test
        void rethrow_exception_when_cipher_initialization_fails_with_NoSuchPaddingException() throws Exception {
            // Given
            NoSuchPaddingException noSuchPaddingException = new NoSuchPaddingException();
            when(cipherInitializer.prepareAndInitCipher(anyInt(), anyString())).thenThrow(noSuchPaddingException);

            // When
            Throwable throwable = catchThrowable(() -> spiedLocalDateCryptoConverter.convertToDatabaseColumn(LOCAL_DATE_TO_CIPHER));

            // Then
            assertThat(throwable).isInstanceOf(RuntimeException.class).hasCause(noSuchPaddingException);
        }

        @Test
        void rethrow_exception_when_cipher_initialization_fails_with_InvalidAlgorithmParameterException() throws Exception {
            // Given
            InvalidAlgorithmParameterException invalidAlgorithmParameterException = new InvalidAlgorithmParameterException();
            when(cipherInitializer.prepareAndInitCipher(anyInt(), anyString())).thenThrow(invalidAlgorithmParameterException);

            // When
            Throwable throwable = catchThrowable(() -> spiedLocalDateCryptoConverter.convertToDatabaseColumn(LOCAL_DATE_TO_CIPHER));

            // Then
            assertThat(throwable).isInstanceOf(RuntimeException.class).hasCause(invalidAlgorithmParameterException);
        }

        @Test
        void rethrow_exception_when_encryption_fails_with_BadPaddingException() throws Exception {
            // Given
            BadPaddingException badPaddingException = new BadPaddingException();
            when(spiedLocalDateCryptoConverter.callCipherDoFinal(any(), any())).thenThrow(badPaddingException);

            // When
            Throwable throwable = catchThrowable(() -> spiedLocalDateCryptoConverter.convertToDatabaseColumn(LOCAL_DATE_TO_CIPHER));

            // Then
            assertThat(throwable).isInstanceOf(RuntimeException.class).hasCause(badPaddingException);
        }

        @Test
        void rethrow_exception_when_encryption_fails_with_IllegalBlockSizeException() throws Exception {
            // Given
            IllegalBlockSizeException illegalBlockSizeException = new IllegalBlockSizeException();
            when(spiedLocalDateCryptoConverter.callCipherDoFinal(any(), any())).thenThrow(illegalBlockSizeException);

            // When
            Throwable throwable = catchThrowable(() -> spiedLocalDateCryptoConverter.convertToDatabaseColumn(LOCAL_DATE_TO_CIPHER));

            // Then
            assertThat(throwable).isInstanceOf(RuntimeException.class).hasCause(illegalBlockSizeException);
        }
    }

    @Nested
    class ConvertToEntityAttributeShould {

        @Test
        void return_null_local_date_when_string_to_decrypt_is_null() {
            // Given
            String dbData = null;

            // When
            LocalDate result = localDateCryptoConverter.convertToEntityAttribute(dbData);

            // Then
            assertThat(result).isNull();
        }

        @Test
        void return_null_local_date_when_string_to_decrypt_is_empty() {
            // Given
            String dbData = "";

            // When
            LocalDate result = localDateCryptoConverter.convertToEntityAttribute(dbData);

            // Then
            assertThat(result).isNull();
        }

        @Test
        void return_decrypted_string() throws Exception {
            // Given
            Cipher cipher = mock(Cipher.class);
            when(cipherInitializer.prepareAndInitCipher(Cipher.DECRYPT_MODE, KeyProperty.DATABASE_ENCRYPTION_KEY)).thenReturn(cipher);

            // When
            LocalDate result = spiedLocalDateCryptoConverter.convertToEntityAttribute(LOCAL_DATE_TO_DECIPHER_AS_STRING);

            // Then
            verify(spiedLocalDateCryptoConverter).callCipherDoFinal(cipher, LOCAL_DATE_TO_CIPHER_AS_STRING.getBytes());
            assertThat(result).isEqualTo(LOCAL_DATE_TO_CIPHER);
        }

        @Test
        void return_unchanged_local_date_when_database_encryption_key_is_null() {
            // Given
            KeyProperty.DATABASE_ENCRYPTION_KEY = null;

            // When
            LocalDate result = spiedLocalDateCryptoConverter.convertToEntityAttribute(LOCAL_DATE_TO_CIPHER_AS_STRING);

            // Then
            assertThat(result).isEqualTo(LOCAL_DATE_TO_CIPHER);
        }

        @Test
        void return_unchanged_local_date_when_database_encryption_key_is_empty() {
            // Given
            KeyProperty.DATABASE_ENCRYPTION_KEY = "";

            // When
            LocalDate result = spiedLocalDateCryptoConverter.convertToEntityAttribute(LOCAL_DATE_TO_CIPHER_AS_STRING);

            // Then
            assertThat(result).isEqualTo(LOCAL_DATE_TO_CIPHER);
        }

        @Test
        void rethrow_exception_when_cipher_initialization_fails_with_InvalidKeyException() throws Exception {
            // Given
            InvalidKeyException invalidKeyException = new InvalidKeyException();
            when(cipherInitializer.prepareAndInitCipher(anyInt(), anyString())).thenThrow(invalidKeyException);

            // When
            Throwable throwable = catchThrowable(() -> spiedLocalDateCryptoConverter.convertToEntityAttribute(LOCAL_DATE_TO_DECIPHER_AS_STRING));

            // Then
            assertThat(throwable).isInstanceOf(RuntimeException.class).hasCause(invalidKeyException);
        }

        @Test
        void rethrow_exception_when_cipher_initialization_fails_with_NoSuchAlgorithmException() throws Exception {
            // Given
            NoSuchAlgorithmException noSuchAlgorithmException = new NoSuchAlgorithmException();
            when(cipherInitializer.prepareAndInitCipher(anyInt(), anyString())).thenThrow(noSuchAlgorithmException);

            // When
            Throwable throwable = catchThrowable(() -> spiedLocalDateCryptoConverter.convertToEntityAttribute(LOCAL_DATE_TO_DECIPHER_AS_STRING));

            // Then
            assertThat(throwable).isInstanceOf(RuntimeException.class).hasCause(noSuchAlgorithmException);
        }

        @Test
        void rethrow_exception_when_cipher_initialization_fails_with_NoSuchPaddingException() throws Exception {
            // Given
            NoSuchPaddingException noSuchPaddingException = new NoSuchPaddingException();
            when(cipherInitializer.prepareAndInitCipher(anyInt(), anyString())).thenThrow(noSuchPaddingException);

            // When
            Throwable throwable = catchThrowable(() -> spiedLocalDateCryptoConverter.convertToEntityAttribute(LOCAL_DATE_TO_DECIPHER_AS_STRING));

            // Then
            assertThat(throwable).isInstanceOf(RuntimeException.class).hasCause(noSuchPaddingException);
        }

        @Test
        void rethrow_exception_when_cipher_initialization_fails_with_InvalidAlgorithmParameterException() throws Exception {
            // Given
            InvalidAlgorithmParameterException invalidAlgorithmParameterException = new InvalidAlgorithmParameterException();
            when(cipherInitializer.prepareAndInitCipher(anyInt(), anyString())).thenThrow(invalidAlgorithmParameterException);

            // When
            Throwable throwable = catchThrowable(() -> spiedLocalDateCryptoConverter.convertToEntityAttribute(LOCAL_DATE_TO_DECIPHER_AS_STRING));

            // Then
            assertThat(throwable).isInstanceOf(RuntimeException.class).hasCause(invalidAlgorithmParameterException);
        }

        @Test
        void rethrow_exception_when_decryption_fails_with_BadPaddingException() throws Exception {
            // Given
            BadPaddingException badPaddingException = new BadPaddingException();
            when(spiedLocalDateCryptoConverter.callCipherDoFinal(any(), any())).thenThrow(badPaddingException);

            // When
            Throwable throwable = catchThrowable(() -> spiedLocalDateCryptoConverter.convertToEntityAttribute(LOCAL_DATE_TO_DECIPHER_AS_STRING));

            // Then
            assertThat(throwable).isInstanceOf(RuntimeException.class).hasCause(badPaddingException);
        }

        @Test
        void rethrow_exception_when_decryption_fails_with_IllegalBlockSizeException() throws Exception {
            // Given
            IllegalBlockSizeException illegalBlockSizeException = new IllegalBlockSizeException();
            when(spiedLocalDateCryptoConverter.callCipherDoFinal(any(), any())).thenThrow(illegalBlockSizeException);

            // When
            Throwable throwable = catchThrowable(() -> spiedLocalDateCryptoConverter.convertToEntityAttribute(LOCAL_DATE_TO_DECIPHER_AS_STRING));

            // Then
            assertThat(throwable).isInstanceOf(RuntimeException.class).hasCause(illegalBlockSizeException);
        }
    }
}
