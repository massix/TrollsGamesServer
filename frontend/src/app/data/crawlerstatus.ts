export class CrawlerStatus {
    running: boolean;
    cacheHit: number;
    cacheMiss: number;
    usersToCrawl: string[];
    gamesLeft: number;
    started: string;
    finished: string;
}