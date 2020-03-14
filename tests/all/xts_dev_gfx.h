/**
 * Screen drawing (gfx) routines
 * 
 * 
 * Xtase - fgalliat @Mar 2020
 * 
 */

class VideoCard {

    private:
        uint16_t mapColor(uint16_t color);

    public:
        VideoCard();
        bool setup();

        int getScreenWidth();
        int getScreenHeight();

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

        // TODO : add drawGlyph(char, fg, bg, transparent, .....)

        // =====================

        bool drawBitmapFile(char* bmpFile, int x, int y, bool rotated=true);
        bool drawPakFile(char* pakFile, int x, int y, int numImage);
        bool drawPctFile(char* pctFile, int x, int y);

        // =====================

        bool loadPCTSpriteBoard(int spriteBoardNum, char* filename);
        bool loadBMPSpriteBoard(int spriteBoardNum, char* filename);

        bool drawSprite(int spriteBoardNum, int xDest, int yDest, int xSrc, int ySrc, int wSrc, int hSrc, bool transparent=false, uint16_t transparentColor=0);
};