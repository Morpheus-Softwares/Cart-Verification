package morpheus.softwares.cartverification.Models;

public class Links {
    private final String link = "https://script.google.com/macros/s/AKfycbwg1mk60bY4azxw-FW1US29RWN_NNWSXVoIzVSsVB-Df90Ng-kjEj-G695x9MHLNRg8/exec";

    public Links() {
    }

    /**
     * Returns a link to the Sheet that contains all synchronized records.
     */
    public String getDATABASEURL() {
        return link;
    }

    /**
     * Returns a link to the JSON Array containing the NPC IDs to be parsed.
     */
    public String getIDSJSONURL() {
        return link + "?action=getIDs";
    }

    /**
     * Returns a link to the JSON Array containing the device's SN to be parsed.
     */
    public String getPRODUCTSJSONURL() {
        return link + "?action=getProducts";
    }
}