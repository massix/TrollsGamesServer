package rocks.massi.data.boardgamegeek;

import lombok.Getter;
import lombok.ToString;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@Getter
@ToString
@XmlRootElement(name = "boardgames")
public class Boardgames {

    @Getter
    @ToString
    public static class Boardgame {

        @XmlAttribute(name = "objectid")
        private int id;

        @XmlElement(name = "yearpublished")
        private int yearPublished;

        @XmlElement
        private String description;

        @XmlElement
        private String thumbnail;

        @XmlElement
        private String image;

        @XmlElement(name = "boardgamehonor")
        private List<String> honors;

        @XmlElement(name = "boardgamedesigner")
        private List<String> designers;

        @XmlElement(name = "name")
        private List<String> alternativeNames;

        @XmlElement
        private Statistics statistics;
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
        private long value;

        @XmlAttribute(name = "bayesaverage")
        private double bayesAverage;
    }

    @XmlElement
    private List<Boardgame> boardgame;
}

