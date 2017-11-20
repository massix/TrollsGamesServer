import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { PagesInformation } from '../data/pagesinformation';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Game } from '../data/game';
import { CollectionInformation } from '../data/collectioninformation';

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
}