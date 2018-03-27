import { Component, OnInit } from '@angular/core';
import { CacheService } from '../services/cache.service';
import { AlertService } from '../services/alert.service';
import { Cache } from '../data/cache';
import { GamesService } from '../services/games.service';
import { Game } from '../data/game';

@Component({
    templateUrl: '../views/cache.component.html'
})
export class CacheComponent implements OnInit {
    memoryCache: Cache;
    focusedGame: Game;

    ngOnInit() {
        this.getMemoryCache();
    }

    getMemoryCache() {
        this.memoryCache = null;
        this.cacheService.getMemoryCache().subscribe(data => this.memoryCache = data);
    }

    focusGame(gameId: number) {
        if (this.focusedGame != null && this.focusedGame.id === gameId) {
            this.focusedGame = null;
        } else {
            this.gamesService.getGame(gameId).subscribe(game => this.focusedGame = game);
        }
    }

    purgeExpired() {
        this.cacheService.purgeExpired().subscribe(
            operation => {
                if (operation.success) {
                    this.alertService.success('Purged expired games from cache');
                    this.getMemoryCache();
                } else {
                    this.alertService.error('Error occured ' + operation.error);
                }
            }
        );
    }

    purgeAll() {
        this.cacheService.purgeAll().subscribe(
            operation => {
                if (operation.success) {
                    this.alertService.success('Purged whole cache');
                    this.getMemoryCache();
                }
            }
        );
    }

    constructor(private cacheService: CacheService,
        private gamesService: GamesService,
        private alertService: AlertService) {}
}
