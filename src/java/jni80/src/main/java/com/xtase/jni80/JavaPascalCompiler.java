package com.xtase.jni80;

import java.io.IOException;

public class JavaPascalCompiler {

    public JavaPascalCompiler() {
    }

    // ex. C:JUKE.PAS
    public boolean compile(String cpmFile) throws IOException {
        return compile(cpmFile, true);
    }

    public boolean compile(String cpmFile, boolean inMemOnly) throws IOException {
        return JavaRunCPM_inMEM.compilePascalPrg(cpmFile, inMemOnly);
    }

    public static void main(String[] args) throws Exception {
        String fileToCompile = null;
        if (args.length < 1) {
            fileToCompile = "C:BMP.PAS";
        } else {
            fileToCompile = args[0];
        }

        new JavaPascalCompiler().compile(fileToCompile);
    }

}