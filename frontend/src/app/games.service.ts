import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { Game } from './game';
import { PagesInformation } from './pagesinformation';

@Injectable()
export class GamesService {
    apiBase = 'http://localhost:8180';

    getGame(id: number): Observable<Game> {
        return this.httpClient.get<Game>(this.apiBase + '/v1/games/get/' + id);
    }

    getGames(): Observable<Game[]> {
        return this.httpClient.get<Game[]>(this.apiBase + '/v1/games/get');
    }

    getTotalPages(): Observable<PagesInformation> {
        return this.httpClient.get<PagesInformation>(this.apiBase + '/v1/games/get/page/total');
    }

    getPageNumber(page: number): Observable<Game[]> {
        return this.httpClient.get<Game[]>(this.apiBase + '/v1/games/get/page/' + page);
    }

    constructor(private httpClient: HttpClient) {}
}