/**
 * Screen drawing (gfx) routines
 * 
 * 
 * Xtase - fgalliat @Mar 2020
 * 
 */

class VideoCard {

    private:
        // uint16_t mapColor(uint16_t color);

    public:
        VideoCard();
        bool setup();

        uint16_t mapColor(uint16_t color);

        int getScreenWidth();
        int getScreenHeight();

        void setRotated(bool r);

        void cls();
        void fillScreen(uint16_t color);

        void drawRect(int x, int y, int w, int h, uint16_t color);
        void fillRect(int x, int y, int w, int h, uint16_t color);
        // if scanW is < 0 -> scanW = w
        void fillRect(int x, int y, int w, int h, uint16_t* colors, int scanW=-1, int offset=0);

        void drawCircle(int x, int y, int radius, uint16_t color);
        void fillCircle(int x, int y, int radius, uint16_t color);


        void drawLine(int x, int y, int x2, int y2, uint16_t color);

        void drawPixel(int x, int y, uint16_t color);

        void drawGlyph(char ch, int x, int y, uint16_t color, uint16_t bgColor);

        // =====================

        bool drawBitmapFile(char* bmpFile, int x, int y, bool rotated=true);
        bool drawPakFile(char* pakFile, int x, int y, int numImage);
        bool drawPctFile(char* pctFile, int x, int y);

        // =====================

        // bool loadPCTSpriteBoard(char* filename);
        bool loadBMPSpriteBoard(char* filename);
        bool defineSprite(int spriteNum, int x, int y, int w, int h);
        bool drawSprite(int spriteNum, int x, int y);

        uint16_t color565(uint8_t r, uint8_t g, uint8_t b);
};