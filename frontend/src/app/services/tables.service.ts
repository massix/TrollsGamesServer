import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { Table } from '../data/table';
import { environment } from '../../environments/environment';

@Injectable()
export class TablesService {
    constructor(private httpClient: HttpClient) {}

    getTables(): Observable<Table[]> {
        return this.httpClient.get<Table[]>(environment.apiBase + '/v1/tables/get');
    }

    createTable(table: Table): Observable<Table> {
        const headers = new HttpHeaders().set('Authorization', 'Bearer ' + localStorage.getItem('token'));
        return this.httpClient.put<Table>(environment.apiBase + '/v1/tables/create', table, {headers: headers});
    }
}