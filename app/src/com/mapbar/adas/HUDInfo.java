package com.mapbar.adas;

import java.util.List;

public class HUDInfo {
    private String type;
    private List<HUDItem> hudItems;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<HUDItem> getHudItems() {
        return hudItems;
    }

    public void setHudItems(List<HUDItem> hudItems) {
        this.hudItems = hudItems;
    }

    @Override
    public String toString() {
        return "HUDInfo{" +
                "type='" + type + '\'' +
                ", hudItems=" + hudItems +
                '}';
    }

    static class HUDItem {

        private String name;
        private int type;
        private boolean supportTire;
        private boolean supportOBD;
        private boolean choice;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public boolean isSupportTire() {
            return supportTire;
        }

        public void setSupportTire(boolean supportTire) {
            this.supportTire = supportTire;
        }

        public boolean isSupportOBD() {
            return supportOBD;
        }

        public void setSupportOBD(boolean supportOBD) {
            this.supportOBD = supportOBD;
        }

        public boolean isChoice() {
            return choice;
        }

        public void setChoice(boolean choice) {
            this.choice = choice;
        }

        @Override
        public String toString() {
            return "HUDItem{" +
                    "name='" + name + '\'' +
                    ", type=" + type +
                    ", supportTire=" + supportTire +
                    ", supportOBD=" + supportOBD +
                    '}';
        }
    }


}
