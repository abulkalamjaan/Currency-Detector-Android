package com.example.muhammadali.note.currencies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class PKR {

    /**
     * this class is final and having singleton instance.
     * this class is data set of Pakistani Currencies
     * And having some logics for extracting important text from detected text
     */

    private static PKR pkr;
    private static final Map<String, String> notes = new HashMap<>(); // data map of notes

    private static final ArrayList<String> keys = new ArrayList<>();

    private PKR() {
        //adding notes data
        //10 rupees data
        notes.put("10", "Ten Rupees");
        notes.put("TEN", "Ten Rupees");
        // 20 rupees data
        notes.put("20", "Twenty Rupees");
        notes.put("TWENTY", "Twenty Rupees");
        //50 rupees data
        notes.put("50", "Fifty Rupees");
        notes.put("FIFTY", "Fifty Rupees");
        //100 rupees
        notes.put("100", "One Hundred Rupees");
        notes.put("ONE HUNDRED", "One Hundred Rupees");
        //500 rupees
        notes.put("500", "Five Hundred Rupees");
        notes.put("FIVE HUNDRED", "Five Hundred Rupees");
        //1000 rupees
        notes.put("1000", "One Thousand Rupees");
        notes.put("ONE THOUSAND", "On Thousand Rupees");
        //5000
        notes.put("5000", "Five Thousand Rupees");
        notes.put("FIVE THOUSAND", "Five Thousand Rupees");


        keys.add("10");
        keys.add("TEN");
        // 20 rupees data
        keys.add("20");
        keys.add("TWENTY");
        //50 rupees data
        keys.add("50");
        keys.add("FIFTY");
        //100 rupees
        keys.add("100");
        keys.add("ONE HUNDRED");
        //500 rupees
        keys.add("500");
        keys.add("FIVE HUNDRED");
        //1000 rupees
        keys.add("1000");
        keys.add("ONE THOUSAND");
        //5000
        keys.add("5000");
        keys.add("FIVE THOUSAND");
    }

    //singleton pattern object
    public static synchronized PKR getInstance() {
        if (pkr == null)
            pkr = new PKR();
        return pkr;
    }

    public String liveNoteDetection(final String data){
        String r="";
        for(String key: keys){
            if(data.contains(key))
                r=notes.get(key);
        }
        return r;
    }

    //method to detect keys inside the data string
    public String whichNoteIsIt(final String data) {
        //split data when space occurs
        String[] splited = data.split("\\s+");
        String r = "";//empty
        //check keys one by one
        for (String key : keys) {
            //check splited data one by one if it matches with key
            for(String value: splited){
               if(value.equalsIgnoreCase(key))
                   return notes.get(key);
            }
        }
        return r;
    }

    //Restriction for notes
    public Map<String, Object> noteLandmarks() {
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.STAMP,"STATE BANK OF PAKISTAN");
        map.put(Constants.SERIAL,7);
        map.put(Constants.CHARACTERS,2);
        return map;
    }
}
