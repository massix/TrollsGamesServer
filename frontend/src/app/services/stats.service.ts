import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { ServerInformation } from '../data/serverinformation';
import { environment } from '../../environments/environment';
import { Stats } from '../data/stats';

@Injectable()
export class StatsService {
    constructor(private httpClient: HttpClient) {}

    getServerInformation(): Observable<ServerInformation> {
        return this.httpClient.get<ServerInformation>(environment.apiBase + '/v1/server/information');
    }

    getServerStatistics(): Observable<Stats[]> {
        const headers = new HttpHeaders().set('Authorization', 'Bearer ' + localStorage.getItem('token'));
        return this.httpClient.get<Stats[]>(environment.apiBase + '/v1/server/stats', {headers: headers});
    }
}
