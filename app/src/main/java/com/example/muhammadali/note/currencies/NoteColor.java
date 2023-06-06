package com.example.muhammadali.note.currencies;

import java.util.HashMap;
import java.util.Map;

public final class NoteColor {
    private static NoteColor color;
    private Map<String, ColorModel> map;

    /**
     * NoteColor class which has data set of note colors and their ranges
     * This class basically detects color confidence of a note
     * Class is singleton
     * */
    private NoteColor() {
        /**key values is the result value of note detected
         * These values are taken from official website State Bank Of Pakistan
         * http://www.sbp.org.pk/BankNotes
         * */
        map = new HashMap<>();
        //ten rupees color ranges
        ColorModel ten = new ColorModel();
        ten.setBlueMax(145);
        ten.setBlueMin(90);
        ten.setGreenMax(255);
        ten.setGreenMin(140);
        ten.setRedMax(150);
        ten.setRedMin(90);

        map.put("Ten Rupees", ten);

        //RS 20
        ColorModel t20 = new ColorModel();
        t20.setGreenMin(115);
        t20.setGreenMax(190);
        t20.setRedMin(140);
        t20.setRedMax(225);
        t20.setBlueMin(40);
        t20.setBlueMax(150);
        map.put("Twenty Rupees", t20);

        //RS 50
        ColorModel fifty = new ColorModel();
        fifty.setBlueMax(155);
        fifty.setBlueMin(90);
        fifty.setRedMax(155);
        fifty.setRedMin(125);
        fifty.setGreenMax(200);
        fifty.setGreenMin(120);
        map.put("Fifty Rupees", fifty);
        //100 Rs
        ColorModel hundred = new ColorModel();
        hundred.setRedMin(170);
        hundred.setRedMax(220);
        hundred.setGreenMin(90);
        hundred.setGreenMax(130);
        hundred.setBlueMin(100);
        hundred.setBlueMax(135);
        map.put("One Hundred Rupees", hundred);

        //500
        ColorModel f00 = new ColorModel();
        f00.setBlueMax(155);
        f00.setBlueMin(100);
        f00.setRedMax(200);
        f00.setRedMin(110);
        f00.setGreenMax(195);
        f00.setGreenMin(115);
        map.put("Five Hundred Rupees", f00);

        //1000
        ColorModel _1000 = new ColorModel();
        _1000.setGreenMin(50);
        _1000.setGreenMax(150);
        _1000.setRedMin(50);
        _1000.setRedMax(160);
        _1000.setBlueMin(180);
        _1000.setBlueMax(225);
        map.put("One Thousand Rupees", _1000);

        //5000
        ColorModel _5000 = new ColorModel();
        _5000.setBlueMax(219);
        _5000.setBlueMin(150);
        _5000.setRedMax(255);
        _5000.setRedMin(180);
        _5000.setGreenMax(90);
        _5000.setGreenMin(10);
        map.put("Five Thousand Rupees", _5000);

    }

    static synchronized NoteColor getInstance() {
        if (color == null)
            color = new NoteColor();
        return color;
    }

    // get confidence of color  which returns %
    float getColorConfidence(final String note, ColorModel model) {
        ColorModel color = map.get(note);
        float total = 30;// total confidence
        float confidence = 3;// common confidence

        if (color != null) {
            //amount of red
            boolean case1 = model.getRedMax() > color.getRedMin() || model.getRedMax() <= color.getRedMax();
            //amount of green
            boolean case2 = model.getGreenMax() > color.getGreenMin() || model.getGreenMax() <= color.getGreenMax();
            //amount of blue
            boolean case3 = model.getBlueMax() > color.getBlueMin() || model.getBlueMax() <= color.getBlueMax();
            //each case true add +9 in confidence
            if (case1)
                confidence += 9;
            else if (case2)
                confidence += 9;
            else if (case3)
                confidence += 9;

            //return %

            return (confidence / total * 100);
        } else
            return 1;
        // result is null 1 confidence
    }

}
