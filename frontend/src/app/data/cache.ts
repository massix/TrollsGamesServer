export class CacheEntry {
    key: number;
    timestamp: number;
    humanReadable: string;
}

export class Cache {
    success: boolean;
    error: string;
    entries: CacheEntry[];
}