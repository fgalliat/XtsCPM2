/**
 * 
 * YATDB FileSystem API 
 * 
 * Xtase - fgalliat @Mar 2020
 */

class Fs {
    public:

        Fs();
        bool setup();

        char* getDiskFileName(char* diskFileName);
        char* getAssetsFileEntry(char* assetName);

        int readTextFile(char* fileName, char* dest, int maxLen);
        // stops on 0x00 (because of TEXTFile)
        int writeTextFile(char* fileName, char* source, int maxLen);
};
