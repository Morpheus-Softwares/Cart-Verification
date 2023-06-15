package morpheus.softwares.cartverification.Models;

public class Links {
    private final String link = "https://script.google.com/macros/s/AKfycbydRm6HfDTjDog6QY_WHRk75uOYWi2xq_7kMsOdX0Qnmg0jESWsJsnOtJeWOC5VPU3I/exec";

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
     * Returns a link to the JSON Array containing the device's ID to be parsed.
     */
    public String getPRODUCTSJSONURL() {
        return link + "?action=getProducts";
    }
}