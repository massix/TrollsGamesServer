export class CrawlerStatus {
    running: boolean;
    cacheHit: number;
    cacheMiss: number;
    usersToCrawl: string[];
    gamesLeft: number;
    ownershipsLeft: number;
    started: string;
    finished: string;
}