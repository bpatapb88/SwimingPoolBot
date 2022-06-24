package com.simanov.pools;

public class Kohoutovice extends ZaLuzankami {
    private static final String URL_KOHOUTOVICE = "https://aqpark.sportujemevbrne.cz/rezervace/";

    public Kohoutovice(String poolName) {
        super(poolName);
    }

    @Override
    public String getUrlStr(){
        return URL_KOHOUTOVICE;
    }
}
