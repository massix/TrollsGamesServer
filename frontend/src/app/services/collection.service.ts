import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { PagesInformation } from '../data/pagesinformation';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Game } from '../data/game';
import { CollectionInformation } from '../data/collectioninformation';
import { Ownership } from '../data/ownership';

@Injectable()
export class CollectionService {
    constructor(private httpClient: HttpClient) {}

    getPagesForUser(user: string): Observable<PagesInformation> {
        return this.httpClient.get<PagesInformation>(environment.apiBase + '/v1/collection/get/' + user + '/page/total');
    }

    getPageForUser(user: string, page: number): Observable<Game[]> {
        return this.httpClient.get<Game[]>(environment.apiBase + '/v1/collection/get/' + user + '/page/' + page);
    }

    getTotalGamesForUser(user: string) : Observable<CollectionInformation> {
        return this.httpClient.get<CollectionInformation>(environment.apiBase + '/v1/collection/get/' + user + '/total');
    }

    removeGameForUser(user: string, game: number): Observable<any> {
        const headers = new HttpHeaders().set('Authorization', 'Bearer ' + localStorage.getItem('token'));
        return this.httpClient.delete<any>(environment.apiBase + '/v1/collection/remove/' + user + '/' + game, {headers: headers});
    }

    addGameForUser(user: string, game: number): Observable<any> {
        const headers = new HttpHeaders().set('Authorization', 'Bearer ' + localStorage.getItem('token'));
        return this.httpClient.put<any>(environment.apiBase + '/v1/collection/add/' + user + '/' + game, null, {headers: headers});
    }

    searchGame(query: string): Observable<Game[]> {
        return this.httpClient.get<Game[]>(environment.apiBase + '/v1/bggconverter/search?q=' + query);
    }
}