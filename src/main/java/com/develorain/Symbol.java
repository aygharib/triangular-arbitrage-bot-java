package com.develorain;

public class Symbol {
    public String symbolString;
    public String baseAsset;
    public String quoteAsset;

    public double askPrice;
    public double bidPrice;

    public double askQuantity;
    public double bidQuantity;

    public double tradeQuantity;

    public boolean temporarilyParsable;

    public Symbol(String symbolString, double askPrice, double bidPrice, double askQuantity, double bidQuantity) {
        this.symbolString = symbolString;
        this.temporarilyParsable = symbolString.length() == 6;

        if (temporarilyParsable) {
            // Get asset codes for base and quote
            // REPLACE THIS WITH REGEX
            this.baseAsset = symbolString.substring(0, 3);
            this.quoteAsset = symbolString.substring(3, 6);

            this.askPrice = askPrice;
            this.bidPrice = bidPrice;

            this.askQuantity = askQuantity;
            this.bidQuantity = bidQuantity;
        }
    }
}
