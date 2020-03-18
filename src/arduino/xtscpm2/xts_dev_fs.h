/**
 * 
 * YATDB FileSystem API 
 * 
 * Xtase - fgalliat @Mar 2020
 */

#define FS_CLASS SdFat

class Fs {
    public:

        Fs();
        bool setup();

        char* getDiskFileName(char* diskFileName);
        char* getAssetsFileEntry(char* assetName);

        bool  eraseFile(char* fileName);

        // (!!) doesn't eraese dest buffer
        int readBinFile(char* fileName, uint8_t* dest, int maxLen);

        int readTextFile(char* fileName, char* dest, int maxLen);
        // stops on 0x00 (because of TEXTFile)
        int writeTextFile(char* fileName, char* source, int maxLen);
};
