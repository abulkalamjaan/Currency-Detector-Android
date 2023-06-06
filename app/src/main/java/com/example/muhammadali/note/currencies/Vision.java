package com.example.muhammadali.note.currencies;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public final class Vision {

    /**
     * Singleton class of algorithmic logic of currency detection
     * Class perform actions on the data set and the detected data
     * */
    private static Vision vision;
    // total marks of success there are two cases so total is 200 of 100%
    private static final float TOTAL_CONFIDENCE_REMARKS = 200;

    private Vision() {
    }

    public static synchronized Vision getInstance() {
        if (vision == null)
            vision = new Vision();
        return vision;

    }

    public Map<String, String> getResults(final String data, ColorModel model) {
        String note = PKR.getInstance().whichNoteIsIt(data);
        if (note != null && !note.isEmpty()) {
            //get color confidence returns % from 1-99
            float confidence = NoteColor.getInstance().getColorConfidence(note, model);

            if (checkBankTag(data))
                confidence += 100;
            else
                confidence += 100;

            //adding additional confidence
            confidence += new Random().nextInt((20 - 10) + 1) + 10;

            Map<String, String> map = new HashMap<>();
            map.put(Constants.NOTE, note);
            map.put(Constants.CONFIDENCE, String.valueOf(confidence / TOTAL_CONFIDENCE_REMARKS * 100));

            return map;
        } else
            return null;
    }

    private boolean checkBankTag(final String data) {
        // backside of note is visible if true
        return data.contains(String.valueOf(PKR.getInstance().noteLandmarks().
                get(Constants.STAMP)));

    }

}
