package rocks.massi.data.boardgamegeek;

import lombok.Getter;
import lombok.ToString;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@ToString
@Getter
@XmlRootElement(name = "items")
public class Collection {

    @Getter
    @ToString
    public static class Item {
        @XmlAttribute(name = "objecttype")
        private String objectType;

        @XmlAttribute(name = "objectid")
        private int id;

        @XmlAttribute(name = "subtype")
        private String subType;

        @XmlElement
        private Status status;
    }

    @Getter
    @ToString
    public static class Status {
        @XmlAttribute
        private boolean own;

        @XmlAttribute(name = "wanttoplay")
        private boolean want;
    }

    @XmlAttribute(name = "totalitems")
    private int totalItems;

    @XmlElement(name = "item")
    private List<Item> itemList;

    public String ownedAsString() {
        ArrayList<String> integers = new ArrayList<>();
        getItemList().forEach(item -> {
            if (item.getSubType().equals("boardgame") && item.getStatus().isOwn()) {
                integers.add(String.valueOf(item.getId()));
            }
        });

        return String.join(" ", integers);
    }

    public String wantedAsString() {
        ArrayList<String> integers = new ArrayList<>();
        getItemList().forEach(item -> {
            if (item.getSubType().equals("boardgame") && item.getStatus().isWant()) {
                integers.add(String.valueOf(item.getId()));
            }
        });

        return String.join(" ", integers);
    }
}
