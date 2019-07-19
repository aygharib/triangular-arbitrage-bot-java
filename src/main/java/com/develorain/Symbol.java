package com.develorain;

public class Symbol {
    public String symbolString;
    public String baseAsset;
    public String quoteAsset;

    public boolean temporarilyParsable;

    public Symbol(String symbolString) {
        this.symbolString = symbolString;
        this.temporarilyParsable = symbolString.length() == 6;

        if (temporarilyParsable) {
            // Get asset codes for base and quote
            // REPLACE THIS WITH REGEX
            this.baseAsset = symbolString.substring(0, 3);
            this.quoteAsset = symbolString.substring(3, 6);
        }
    }
}
