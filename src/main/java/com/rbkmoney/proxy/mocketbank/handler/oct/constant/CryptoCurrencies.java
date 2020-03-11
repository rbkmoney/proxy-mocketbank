package com.rbkmoney.proxy.mocketbank.handler.oct.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum CryptoCurrencies {

    BITCOIN("Bitcoin", "BTC"),
    BITCOIN_XBT("Bitcoin", "XBT"),
    BITCOIN_CASH("Bitcoin Cash", "BCH"),
    LITECOIN("Litecoin", "LTC"),
    NAMECOIN("Namecoin", "NMC"),
    PEERCOIN("Peercoin", "PPC"),
    DOGECOIN("Dogecoin", "DOGE"),
    DOGECOIN_XDG("Dogecoin", "XDG"),
    GRIDCOIN("Gridcoin", "GRC"),
    PRIMECOIN("Primecoin", "XPM"),
    NXT("Nxt", "NXT"),
    AURORACOIN("Auroracoin", "AUR"),
    DASH("Dash", "DASH"),
    NEO("NEO", "NEO"),
    MAZACOIN("MazaCoin", "MZC"),
    MONERO("Monero", "XMR"),
    NEM("NEM", "XEM"),
    POTCOIN("PotCoin", "POT"),
    TITCOIN("Titcoin", "TIT"),
    VERGE("Verge", "XVG"),
    STELLAR("Stellar", "XLM"),
    VERTCOIN("Vertcoin", "VTC"),
    RIPPLE("Ripple", "XRP"),
    ETHEREUM("Ethereum", "ETH"),
    ETHEREUM_CLASSIC("Ethereum Classic", "ETC"),
    TETHER("Tether", "USDT"),
    EOS_IO("EOS.IO", "EOS"),
    ZCASH("Zcash", "ZEC");

    private final String currency;
    private final String symbol;

    public static String[] toArraySymbol() {
        return Arrays.stream(values())
                .map(CryptoCurrencies::getSymbol)
                .distinct()
                .toArray(String[]::new);
    }
}
