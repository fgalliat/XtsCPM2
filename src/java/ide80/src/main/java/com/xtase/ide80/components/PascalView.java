package com.xtase.ide80.components;

/* Copyright xtase - fgalliat @Apr2020
 *
 * Modified code for TurboPascal3 edition
 * 
 * ----------------------------------------
 *
 * Copyright 2006-2008 Kees de Kooter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// package net.boplicity.xmleditor;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
import javax.swing.text.PlainView;
import javax.swing.text.Segment;
import javax.swing.text.Utilities;

/**
 * Thanks: http://groups.google.com/group/de.comp.lang.java/msg/2bbeb016abad270
 *
 * IMPORTANT NOTE: regex should contain 1 group.
 *
 * Using PlainView here because we don't want line wrapping to occur.
 *
 * @author kees
 * @date 13-jan-2006
 *
 */
public class PascalView extends PlainView {

    private static HashMap<Pattern, Color> patternColors;
    // private static String GENERIC_XML_NAME = "[A-Za-z]+[A-Za-z0-9\\-_]*(:[A-Za-z]+[A-Za-z0-9\\-_]+)?";
    // private static String TAG_PATTERN = "(</?" + GENERIC_XML_NAME + ")";
    // private static String TAG_END_PATTERN = "(>|/>)";
    // private static String TAG_ATTRIBUTE_PATTERN = "(" + GENERIC_XML_NAME + ")\\w*\\=";
    // private static String TAG_ATTRIBUTE_VALUE = "\\w*\\=\\w*(\"[^\"]*\")";
    
    // private static String TAG_CDATA = "(<\\!\\[CDATA\\[.*\\]\\]>)";
    // =========================================>
    private static String commonText = "[^\\}]*"; // all except '}'

    private static String TAG_INCLUDE = "(\\{(\\$I )"+commonText+"\\})";
    private static String TAG_COMMENT = "(\\{"+commonText+"\\})";
    private static String TYPE_DATA = "(integer|char|byte|short|real|boolean|string)";
    private static String TYPE_FCTPRC = "(function|procedure|program)";
    private static String TYPE_BEGEND = "(begin|end)";
    private static String TYPE_COND = "(if|else|then)";
    private static String TYPE_FLOW = "(while| do |repeat|until|for )";
    private static String TYPE_NUM = "[ ,=\\(\\*+\\-\\/)]([0-9\\.]+)";

    // protected Color FOREGROUND_COLOR = Color.black;
    protected Color FOREGROUND_COLOR = new Color( 150, 150, 150 );


    static {
        // NOTE: the order is important!
        patternColors = new LinkedHashMap<Pattern, Color>();

        patternColors.put(Pattern.compile(TAG_INCLUDE), new Color(200, 75, 60) ); // redish
        patternColors.put(Pattern.compile(TAG_COMMENT), new Color(75, 120, 75) ); // greenish dark

        patternColors.put(Pattern.compile(TYPE_DATA, Pattern.CASE_INSENSITIVE), new Color(75, 190, 160) ); // greenish
        patternColors.put(Pattern.compile(TYPE_FCTPRC, Pattern.CASE_INSENSITIVE), new Color(80, 135, 140) ); // blueish dark
        patternColors.put(Pattern.compile(TYPE_BEGEND, Pattern.CASE_INSENSITIVE), new Color(130, 125, 85) ); // greenish light

        patternColors.put(Pattern.compile(TYPE_COND, Pattern.CASE_INSENSITIVE), new Color(130, 100, 140) ); // pinkish
        patternColors.put(Pattern.compile(TYPE_FLOW, Pattern.CASE_INSENSITIVE), new Color(130, 100, 140) ); // pinkish

        patternColors.put(Pattern.compile(TYPE_NUM), new Color(130, 125, 85) ); // greenish light


        // patternColors
        //         .put(Pattern.compile(TAG_PATTERN), new Color(63, 127, 127));
        // patternColors.put(Pattern.compile(TAG_CDATA), Color.GRAY);
        // patternColors.put(Pattern.compile(TAG_ATTRIBUTE_PATTERN), new Color(
        //         127, 0, 127));
        // patternColors.put(Pattern.compile(TAG_END_PATTERN), new Color(63, 127,
        //         127));
        // patternColors.put(Pattern.compile(TAG_ATTRIBUTE_VALUE), new Color(42,
        //         0, 255));
    }

    public PascalView(Element element) {
        super(element);

        // Set tabsize to 4 (instead of the default 8)
        getDocument().putProperty(PlainDocument.tabSizeAttribute, 4);
    }

    // Xts : Overlap detection
    protected class Interval {
        int start;
        int end;
        public Interval(int start, int end) {
            this.start = start;
            this.end = end;
        }


        public boolean overlaps(Interval intv) {
            return intv.start >= this.start && intv.start <= this.end; 
        }
    }

    protected List<Interval> intervals = new ArrayList<>();

    protected boolean hasOverlap(Interval intv) {
        for(Interval interval : intervals) {
            if ( interval.overlaps(intv) ) {
                return true;
            }
        }
        return false;
    }
    // Xts : END

    @Override
    protected int drawUnselectedText(Graphics graphics, int x, int y, int p0,
            int p1) throws BadLocationException {

        Document doc = getDocument();
        String text = doc.getText(p0, p1 - p0);

        Segment segment = getLineBuffer();

        SortedMap<Integer, Integer> startMap = new TreeMap<Integer, Integer>();
        SortedMap<Integer, Color> colorMap = new TreeMap<Integer, Color>();

        intervals.clear();

        // Match all regexes on this snippet, store positions
        for (Map.Entry<Pattern, Color> entry : patternColors.entrySet()) {

            Matcher matcher = entry.getKey().matcher(text);

            while (matcher.find()) {
                
                // Xts : Overlap detection
                Interval intv = new Interval(matcher.start(1), matcher.end());
                if ( hasOverlap(intv) ) { continue; }
                intervals.add(intv);
                // Xts : END

                startMap.put(matcher.start(1), matcher.end());
                colorMap.put(matcher.start(1), entry.getValue());
            }
            
        }

        // TODO: check the map for overlapping parts

        int i = 0;

        // Colour the parts
        for (Map.Entry<Integer, Integer> entry : startMap.entrySet()) {
            int start = entry.getKey();
            int end = entry.getValue();

            if (i < start) {
                graphics.setColor(FOREGROUND_COLOR);
                doc.getText(p0 + i, start - i, segment);
                x = Utilities.drawTabbedText(segment, x, y, graphics, this, i);
            }

            graphics.setColor(colorMap.get(start));
            i = end;
            doc.getText(p0 + start, i - start, segment);
            x = Utilities.drawTabbedText(segment, x, y, graphics, this, start);
        }

        // Paint possible remaining text black
        if (i < text.length()) {
            graphics.setColor(FOREGROUND_COLOR);
            doc.getText(p0 + i, text.length() - i, segment);
            x = Utilities.drawTabbedText(segment, x, y, graphics, this, i);
        }

        return x;
    }

}
