package rocks.massi.data.boardgamegeek;

import lombok.Getter;
import lombok.ToString;
import rocks.massi.data.Game;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import java.util.LinkedList;
import java.util.List;

@Getter
@ToString
@XmlRootElement(name = "boardgames")
public class Boardgames {
    public static class Constants {
        public static int EXPANSION_CATEGORY = 1042;
    }

    @Getter
    @ToString
    public static class Boardgame {

        @XmlAttribute(name = "objectid")
        private int id;

        @XmlElement(name = "yearpublished")
        private int yearPublished;

        @XmlElement(name = "minplayers")
        private int minPlayers;

        @XmlElement(name = "maxplayers")
        private int maxPlayers;

        @XmlElement(name = "playingtime")
        private int playingTime;

        @XmlElement
        private String description;

        @XmlElement
        private String thumbnail;

        @XmlElement
        private String image;

        @XmlElement(name = "boardgamedesigner")
        private List<String> designers;

        @XmlElement(name = "name")
        private List<Name> alternativeNames;

        @XmlElement
        private Statistics statistics;

        @XmlElement(name = "boardgameexpansion")
        private List<BoardgameExpansion> expansion;

        @XmlElement(name = "boardgamecategory")
        private List<BoardgameCategory> categories;

        @XmlElement(name = "boardgamehonor")
        private List<BoardgameHonor> honors;


        public Game convert() {
            String primaryName = "";
            int globalRank = -1;
            boolean isExpansion = false;
            String expands = "";
            String authors = "";

            if (getDesigners() != null)
                authors = String.join(", ", getDesigners());

            for (Name name : getAlternativeNames()) {
                if (name.isPrimary())
                    primaryName = name.getName();
            }

            for (Rank rank : getStatistics().getRatings().getRanks().getRanks()) {
                if ("boardgame".equals(rank.getName())) {
                    try {
                        globalRank = Integer.valueOf(rank.getValue());
                    }
                    catch(Exception e) {
                        // Do nothing
                    }
                }
            }

            if (getCategories() != null) {
                for (BoardgameCategory category : getCategories()) {
                    if (category.getId() == Constants.EXPANSION_CATEGORY)
                        isExpansion = true;
                }
            }

            if (getExpansion() != null) {
                LinkedList<String> expansions = new LinkedList<>();
                for (BoardgameExpansion expansion : getExpansion()) {
                    if (expansion.isInbound()) expansions.add(String.valueOf(expansion.getForGame()));
                }

                expands = String.join(" ", expansions);
            }

            return new Game(getId(), primaryName, getDescription(), getMinPlayers(), getMaxPlayers(),
                    getPlayingTime(), getYearPublished(), globalRank, isExpansion, getThumbnail(), authors, expands);
        }

    }

    @Getter
    @ToString
    public static class Statistics {

        @XmlElement
        private Ratings ratings;
    }

    @Getter
    @ToString
    public static class Ratings {

        @XmlElement
        private Ranks ranks;
    }

    @Getter
    @ToString
    public static class Ranks {

        @XmlElement(name = "rank")
        private List<Rank> ranks;
    }

    @Getter
    @ToString
    public static class Rank {

        @XmlAttribute
        private String type;

        @XmlAttribute
        private long id;

        @XmlAttribute
        private String name;

        @XmlAttribute(name = "friendlyname")
        private String friendlyName;

        @XmlAttribute
        private String value;

        @XmlAttribute(name = "bayesaverage")
        private String bayesAverage;
    }

    @Getter
    @ToString
    public static class Name {
        @XmlAttribute
        private boolean primary;

        @XmlValue
        private String name;
    }

    @Getter
    @ToString
    public static class BoardgameExpansion {
        @XmlAttribute(name = "objectid")
        private int forGame;

        @XmlAttribute
        private boolean inbound;

        @XmlValue
        private String game;
    }

    @Getter
    @ToString
    public static class BoardgameCategory {
        @XmlAttribute(name = "objectid")
        private int id;

        @XmlValue
        private String description;
    }

    @Getter
    @ToString
    public static class BoardgameHonor {
        @XmlAttribute(name = "objectid")
        private int id;

        @XmlValue
        private String description;
    }

    @XmlElement
    private List<Boardgame> boardgame;

}

