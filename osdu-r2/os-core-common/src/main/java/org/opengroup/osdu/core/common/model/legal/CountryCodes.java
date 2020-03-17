/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.core.common.model.legal;

import java.util.HashMap;
import java.util.Map;

public enum CountryCodes
{
    AD("Andorra", "AD", 16),

    AE("United Arab Emirates", "AE", 784),

    AF("Afghanistan", "AF", 4),

    AG("Antigua and Barbuda", "AG", 28),

    AI("Anguilla", "AI", 660),

    AL("Albania", "AL", 8),

    AM("Armenia", "AM", 51),

    AN("Netherlands Antilles", "AN", 530),

    AO("Angola", "AO", 24),

    AQ("Antarctica", "AQ", 10),

    AR("Argentina", "AR", 32, RESIDENCY_RISK.NO_RESTRICTION),

    AS("American Samoa", "AS", 16),

    AT("Austria", "AT", 40),

    AU("Australia", "AU", 36, RESIDENCY_RISK.NO_RESTRICTION),

    AW("Aruba", "AW", 533),

    AX("Aland Islands", "AX", 248),

    AZ("Azerbaijan", "AZ", 31),

    BA("Bosnia and Herzegovina", "BA", 70),

    BB("Barbados", "BB", 52, RESIDENCY_RISK.NOT_ASSIGNED),

    BD("Bangladesh", "BD", 50),

    BE("Belgium", "BE", 56, RESIDENCY_RISK.NOT_ASSIGNED),

    BF("Burkina Faso", "BF", 854),

    BG("Bulgaria", "BG", 100),

    BH("Bahrain", "BH", 48, RESIDENCY_RISK.NOT_ASSIGNED),

    BI("Burundi", "BI", 108),

    BJ("Benin", "BJ", 204, RESIDENCY_RISK.NOT_ASSIGNED),

    BL("Saint Barthelemy", "BL", 652),

    BM("Bermuda", "BM", 60),

    BN("Brunei Darussalam", "BN", 96, RESIDENCY_RISK.NO_RESTRICTION),

    BO("Bolivia", "BO", 68, RESIDENCY_RISK.NO_RESTRICTION),

    BR("Brazil", "BR", 76),

    BS("Bahamas", "BS", 44),

    BT("Bhutan", "BT", 64),

    BV("Bouvet Island", "BV", 74),

    BW("Botswana", "BW", 72),

    BY("Belarus", "BY", 112),

    BZ("Belize", "BZ", 84, RESIDENCY_RISK.NOT_ASSIGNED),

    CA("Canada", "CA", 124, RESIDENCY_RISK.NO_RESTRICTION),

    CC("Cocos Islands", "CC", 166),

    CD("The Democratic Republic of the Congo", "CD", 180),

    CF("Central African Republic", "CF", 140),

    CG("Congo", "CG", 178),

    CH("Switzerland", "CH", 756, RESIDENCY_RISK.NOT_ASSIGNED),

    CI("Cote d'Ivoire", "CI", 384),

    CK("Cook Islands", "CK", 184),

    CL("Chile", "CL", 152, RESIDENCY_RISK.NO_RESTRICTION),

    CM("Cameroon", "CM", 120),

    CN("China", "CN", 156),

    CO("Colombia", "CO", 170),

    CR("Costa Rica", "CR", 188),

    CU("Cuba", "CU", 192, RESIDENCY_RISK.EMBARGOED),

    CV("Cape Verde", "CV", 132),

    CX("Christmas Island", "CX", 162),

    CY("Cyprus", "CY", 196, RESIDENCY_RISK.NOT_ASSIGNED),

    CZ("Czech Republic", "CZ", 203),

    DE("Germany", "DE", 276),

    DJ("Djibouti", "DJ", 262),

    DK("Denmark", "DK", 208, RESIDENCY_RISK.NO_RESTRICTION),

    DM("Dominica", "DM", 212),

    DO("Dominican Republic", "DO", 214, RESIDENCY_RISK.NOT_ASSIGNED),

    DZ("Algeria", "DZ", 12),

    EC("Ecuador", "EC", 218, RESIDENCY_RISK.NO_RESTRICTION),

    EE("Estonia", "EE", 233),

    EG("Egypt", "EG", 818),

    EH("Western Sahara", "EH", 732),

    ER("Eritrea", "ER", 232),

    ES("Spain", "ES", 724, RESIDENCY_RISK.NO_RESTRICTION),

    ET("Ethiopia", "ET", 231, RESIDENCY_RISK.NOT_ASSIGNED),

    FI("Finland", "FI", 246),

    FJ("Fiji", "FJ", 242),

    FK("Falkland Islands", "FK", 238),

    FM("Federated States of Micronesia", "FM", 583),

    FO("Faroe Islands", "FO", 234),

    FR("France", "FR", 250, RESIDENCY_RISK.NO_RESTRICTION),

    GA("Gabon", "GA", 266),

    GB("United Kingdom", "GB", 826, RESIDENCY_RISK.NO_RESTRICTION),

    GD("Grenada", "GD", 308),

    GE("Georgia", "GE", 268, RESIDENCY_RISK.NOT_ASSIGNED),

    GF("French Guiana", "GF", 254),

    GG("Guernsey", "GG", 831),

    GH("Ghana", "GH", 288),

    GI("Gibraltar", "GI", 292),

    GL("Greenland", "GL", 304, RESIDENCY_RISK.NOT_ASSIGNED),

    GM("Gambia", "GM", 270),

    GN("Guinea", "GN", 324),

    GP("Guadeloupe", "GP", 312),

    GQ("Equatorial Guinea", "GQ", 226),

    GR("Greece", "GR", 300),

    GS("South Georgia and the South Sandwich Islands", "GS", 239),

    GT("Guatemala", "GT", 320, RESIDENCY_RISK.NOT_ASSIGNED),

    GU("Guam", "GU", 316),

    GW("Guinea-Bissau", "GW", 624),

    GY("Guyana", "GY", 328, RESIDENCY_RISK.NOT_ASSIGNED),

    HK("Hong Kong", "HK", 344),

    HM("Heard Island and McDonald Islands", "HM", 334),

    HN("Honduras", "HN", 340),

    HR("Croatia", "HR", 191),

    HT("Haiti", "HT", 332),

    HU("Hungary", "HU", 348),

    ID("Indonesia", "ID", 360),

    IE("Ireland", "IE", 372),

    IL("Israel", "IL", 376, RESIDENCY_RISK.NOT_ASSIGNED),

    IM("Isle of Man", "IM", 833),

    IN("India", "IN", 356),

    IO("British Indian Ocean Territory", "IO", 86),

    IQ("Iraq", "IQ", 368),

    IR("Islamic Republic of Iran", "IR", 364, RESIDENCY_RISK.EMBARGOED),

    IS("Iceland", "IS", 352),

    IT("Italy", "IT", 380, RESIDENCY_RISK.NO_RESTRICTION),

    JE("Jersey", "JE", 832),

    JM("Jamaica", "JM", 388),

    JO("Jordan", "JO", 400),

    JP("Japan", "JP", 392, RESIDENCY_RISK.NOT_ASSIGNED),

    KE("Kenya", "KE", 404),

    KG("Kyrgyzstan", "KG", 417),

    KH("Cambodia", "KH", 116),

    KI("Kiribati", "KI", 296),

    KM("Comoros", "KM", 174),

    KN("Saint Kitts and Nevis", "KN", 659),

    KP("Democratic People's Republic of Korea", "KP", 408, RESIDENCY_RISK.EMBARGOED),

    KR("Republic of Korea", "KR", 410, RESIDENCY_RISK.NOT_ASSIGNED),

    KW("Kuwait", "KW", 414),

    KY("Cayman Islands", "KY", 136),

    KZ("Kazakhstan", "KZ", 398),

    LA("Lao People's Democratic Republic", "LA", 418),

    LB("Lebanon", "LB", 422, RESIDENCY_RISK.NO_RESTRICTION),

    LC("Saint Lucia", "LC", 662),

    LI("Liechtenstein", "LI", 438),

    LK("Sri Lanka", "LK", 144, RESIDENCY_RISK.NOT_ASSIGNED),

    LR("Liberia", "LR", 430, RESIDENCY_RISK.NOT_ASSIGNED),

    LS("Lesotho", "LS", 426),

    LT("Lithuania", "LT", 440, RESIDENCY_RISK.NOT_ASSIGNED),

    LU("Luxembourg", "LU", 442),

    LV("Latvia", "LV", 428),

    LY("Libya", "LY", 434),

    MA("Morocco", "MA", 504, RESIDENCY_RISK.NOT_ASSIGNED),

    MC("Monaco", "MC", 492),

    MD("Republic of Moldova", "MD", 498),

    ME("Montenegro", "ME", 499),

    MF("Saint Martin", "MF", 663),

    MG("Madagascar", "MG",450, RESIDENCY_RISK.NOT_ASSIGNED),

    MH("Marshall Islands", "MH", 584),

    MK("The former Yugoslav Republic of Macedonia", "MK", 807),

    ML("Mali", "ML", 466),

    MM("Myanmar", "MM", 104, RESIDENCY_RISK.NOT_ASSIGNED),

    MN("Mongolia", "MN", 496),

    MO("Macao", "MO", 446),

    MP("Northern Mariana Islands", "MP",580),

    MQ("Martinique", "MQ", 474),

    MR("Mauretania", "MR", 478, RESIDENCY_RISK.NOT_ASSIGNED),

    MS("Montserrat", "MS", 500),

    MT("Malta", "MT", 470),

    MU("Mauritius", "MU", 480),

    MV("Maldives", "MV", 462),

    MW("Malawi", "MW", 454),

    MX("Mexico", "MX", 484),

    MY("Malaysia", "MY", 458, RESIDENCY_RISK.CLIENT_CONSENT_REQUIRED),

    MZ("Mozambique", "MZ", 508),

    NA("Namibia", "NA", 516, RESIDENCY_RISK.NOT_ASSIGNED),

    NC("New Caledonia", "NC", 540),

    NE("Niger", "NE", 562),

    NF("Norfolk Island", "NF", 574),

    NG("Nigeria","NG", 566),

    NI("Nicaragua", "NI", 558),

    NL("Netherlands", "NL", 528, RESIDENCY_RISK.NOT_ASSIGNED),

    NO("Norway", "NO", 578, RESIDENCY_RISK.NO_RESTRICTION),

    NP("Nepal", "NP", 524),

    NR("Nauru", "NR", 520),

    NU("Niue", "NU", 570),

    NZ("New Zealand", "NZ", 554, RESIDENCY_RISK.NO_RESTRICTION),

    OM("Oman", "OM", 512),

    PA("Panama", "PA", 591),

    PE("Peru", "PE", 604),

    PF("French Polynesia", "PF", 258),

    PG("Papua New Guinea", "PG", 598, RESIDENCY_RISK.NO_RESTRICTION),

    PH("Philippines", "PH", 608, RESIDENCY_RISK.NOT_ASSIGNED),

    PK("Pakistan", "PK", 586),

    PL("Poland", "PL", 616, RESIDENCY_RISK.NOT_ASSIGNED),

    PM("Saint Pierre and Miquelon", "PM", 666),

    PN("Pitcairn", "PN", 612),

    PR("Puerto Rico", "PR", 630),

    PS("Palestinian Territory", "PS", 275),

    PT("Portugal", "PT", 620),

    PW("Palau", "PW", 585),

    PY("Paraguay", "PY", 600, RESIDENCY_RISK.NOT_ASSIGNED),

    QA("Qatar", "QA", 634),

    RE("Reunion", "RE", 638),

    RO("Romania", "RO", 642),

    RS("Serbia", "RS", 688),

    RU("Russian Federation", "RU", 643, RESIDENCY_RISK.EMBARGOED),

    RW("Rwanda", "RW", 646),

    SA("Saudi Arabia", "SA", 682),

    SB("Solomon Islands", "SB", 90),

    SC("Seychelles", "SC", 690),

    SD("Sudan", "SD", 729, RESIDENCY_RISK.EMBARGOED),

    SE("Sweden", "SE", 752),

    SG("Singapore", "SG", 702, RESIDENCY_RISK.NOT_ASSIGNED),

    SH("Saint Helena", "SH", 654),

    SI("Slovenia", "SI", 705),

    SJ("Svalbard and Jan Mayen", "SJ", 744),

    SK("Slovakia", "SK", 703),

    SL("Sierra Leone", "SL", 694),

    SM("San Marino", "SM", 674),

    SN("Senegal", "SN", 686, RESIDENCY_RISK.NOT_ASSIGNED),

    SO("Somalia", "SO", 706),

    SR("Suriname", "SR", 740, RESIDENCY_RISK.NOT_ASSIGNED),

    SS("South Sudan", "SS", 728, RESIDENCY_RISK.EMBARGOED),

    ST("Sao Tome and Principe", "ST", 678),

    SV("El Salvador", "SV", 222),

    SY("Syrian Arab Republic", "SY", 760, RESIDENCY_RISK.EMBARGOED),

    SZ("Swaziland", "SZ", 748),

    TC("Turks and Caicos Islands", "TC", 796),

    TD("Chad", "TD", 148),

    TG("Togo", "TG", 768, RESIDENCY_RISK.NOT_ASSIGNED),

    TH("Thailand", "TH", 764, RESIDENCY_RISK.NO_RESTRICTION),

    TJ("Tajikistan", "TJ", 762),

    TK("Tokelau", "TK", 772),

    TL("Timor-Leste", "TL", 626),

    TM("Turkmenistan", "TM", 795, RESIDENCY_RISK.NOT_ASSIGNED),

    TN("Tunisia", "TN", 788, RESIDENCY_RISK.NOT_ASSIGNED),

    TO("Tonga", "TO", 776),

    TR("Turkey", "TR", 792),

    TT("Trinidad and Tobago", "TT", 780, RESIDENCY_RISK.NOT_ASSIGNED),

    TV("Tuvalu", "TV", 798),

    TW("Taiwan, Province of China", "TW", 158, RESIDENCY_RISK.NOT_ASSIGNED),

    TZ("United Republic of Tanzania", "TZ", 834),

    UA("Ukraine", "UA", 804),

    UG("Uganda", "UG", 800, RESIDENCY_RISK.NOT_ASSIGNED),

    UM("United States Minor Outlying Islands", "UM", 581),

    US("United States", "US", 840, RESIDENCY_RISK.NO_RESTRICTION),

    UY("Uruguay", "UY", 858),

    UZ("Uzbekistan", "UZ", 860),

    VC("Saint Vincent and the Grenadines", "VC", 670),

    VE("Venezuela", "VE", 862),

    VG("British Virgin Islands", "VG", 92),

    VI("Virgin Islands, U.S.", "VI", 850),

    VN("Vietnam", "VN", 704),

    VU("Vanuatu", "VU", 548),

    WF("Wallis and Futuna", "WF", 876),

    WS("Samoa", "WS", 882),

    YE("Yemen", "YE", 887, RESIDENCY_RISK.NOT_ASSIGNED),

    ZA("South Africa", "ZA", 710),

    ZM("Zambia", "ZM", 894),

    ZW("Zimbabwe", "ZW", 716),

    Default("Default", "XX", 999, RESIDENCY_RISK.DEFAULT)
    ;

    private static final Map<String, CountryCodes> alpha2Map = new HashMap<>();

    static
    {
        for (CountryCodes cc : values())
        {
            alpha2Map.put(cc.getAlpha2(), cc);
        }
    }

    private final String name;
    private final String alpha2;
    private final int numeric;
    private final String residencyRisk;
    public static class RESIDENCY_RISK {
        public static final String NO_RESTRICTION = "No restriction";
        public static final String NOT_ASSIGNED = "Not assigned";
        public static final String EMBARGOED = "Embargoed";
        public static final String DEFAULT = "Default";
        public static final String CLIENT_CONSENT_REQUIRED = "Client consent required";
    }

    CountryCodes(String name, String alpha2, int numeric)
    {
        this(name, alpha2, numeric, RESIDENCY_RISK.DEFAULT);
    }
    CountryCodes(String name, String alpha2, int numeric, String residencyRisk)
    {
        this.name = name;
        this.alpha2 = alpha2;
        this.numeric = numeric;
        this.residencyRisk = residencyRisk;
    }

    public String getName()
    {
        return name;
    }

    public String getAlpha2()
    {
        return alpha2;
    }

    public int getNumeric()
    {
        return numeric;
    }

    public String getResidencyRisk()
    {
        return residencyRisk;
    }

    public boolean needsClientConsent(){
        return getResidencyRisk().equalsIgnoreCase(CountryCodes.RESIDENCY_RISK.CLIENT_CONSENT_REQUIRED);
    }
    public boolean isEmbargoed(){
        return getResidencyRisk().equalsIgnoreCase(RESIDENCY_RISK.EMBARGOED);
    }

    public static CountryCodes getByCode(String code)
    {
        if (code == null)
            return Default;

        CountryCodes country = alpha2Map.get(code);

        return country == null ? Default : country;
    }


}
