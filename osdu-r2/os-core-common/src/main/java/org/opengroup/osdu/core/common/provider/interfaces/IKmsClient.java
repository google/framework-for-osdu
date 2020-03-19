package org.opengroup.osdu.core.common.provider.interfaces;

import java.io.IOException;

public interface IKmsClient {
    public String encryptString(String textToBeEncrypted) throws IOException;
    public String decryptString(String textToBeDecrypted) throws IOException;
}
