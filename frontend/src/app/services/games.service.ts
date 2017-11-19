import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { Game } from '../data/game';
import { PagesInformation } from '../data/pagesinformation';
import { environment } from '../../environments/environment';

@Injectable()
export class GamesService {
    getGame(id: number): Observable<Game> {
        return this.httpClient.get<Game>(environment.apiBase + '/v1/games/get/' + id);
    }

    getGames(): Observable<Game[]> {
        return this.httpClient.get<Game[]>(environment.apiBase + '/v1/games/get');
    }

    getTotalPages(): Observable<PagesInformation> {
        return this.httpClient.get<PagesInformation>(environment.apiBase + '/v1/games/get/page/total');
    }

    getPageNumber(page: number): Observable<Game[]> {
        return this.httpClient.get<Game[]>(environment.apiBase + '/v1/games/get/page/' + page);
    }

    getOwners(gameId: number): Observable<string[]> {
        return this.httpClient.get<string[]>(environment.apiBase + '/v1/games/owners/' + gameId);
    }

    removeGame(gameId: number): Observable<any> {
        const headers = new HttpHeaders().set('Authorization', 'Bearer ' + localStorage.getItem('token'));
        return this.httpClient.delete(environment.apiBase + '/v1/games/remove/' + gameId, {headers: headers});
    }

    recrawlGame(gameId: number): Observable<any> {
        const headers = new HttpHeaders().set('Authorization', 'Bearer ' + localStorage.getItem('token'));
        return this.httpClient.post(environment.apiBase + '/v1/crawler/games/' + gameId, null, {headers: headers});
    }

    searchGame(term: string): Observable<Game[]> {
        return this.httpClient.get<Game[]>(environment.apiBase + '/v1/games/search?q=' + term);
    }

    constructor(private httpClient: HttpClient) {}
}