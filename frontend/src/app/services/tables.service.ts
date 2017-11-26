import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { Table } from '../data/table';
import { environment } from '../../environments/environment';
import { User } from '../data/user';
import { Game } from '../data/game';

@Injectable()
export class TablesService {
    constructor(private httpClient: HttpClient) {}

    getTables(): Observable<Table[]> {
        return this.httpClient.get<Table[]>(environment.apiBase + '/v1/tables/get');
    }

    getTable(id: number): Observable<Table> {
        return this.httpClient.get<Table>(environment.apiBase + '/v1/tables/get/' + id);
    }

    createTable(table: Table): Observable<Table> {
        const headers = new HttpHeaders().set('Authorization', 'Bearer ' + localStorage.getItem('token'));
        return this.httpClient.put<Table>(environment.apiBase + '/v1/tables/create', table, {headers: headers});
    }

    removeTable(table: Table): Observable<Table> {
        const headers = new HttpHeaders().set('Authorization', 'Bearer ' + localStorage.getItem('token'));
        return this.httpClient.delete<Table>(environment.apiBase + '/v1/tables/remove/' + table.id, {headers: headers});
    }

    getUsersForTable(table: Table): Observable<User[]> {
        return this.httpClient.get<User[]>(environment.apiBase + '/v1/rdv/table/' + table.id + '/users');
    }

    getGamesForTable(table: Table): Observable<Game[]> {
        return this.httpClient.get<Game[]>(environment.apiBase + '/v1/rdv/table/' + table.id + '/games');
    }

    addUserToTable(table: Table, user: string): Observable<any> {
        const headers = new HttpHeaders().set('Authorization', 'Bearer ' + localStorage.getItem('token'));
        return this.httpClient.put(environment.apiBase + '/v1/rdv/table/' + table.id + '/add_user/' + user, null, {headers: headers});
    }

    removeUserFromTable(table: Table, user: string): Observable<any> {
        const headers = new HttpHeaders().set('Authorization', 'Bearer ' + localStorage.getItem('token'));
        return this.httpClient.delete(environment.apiBase + '/v1/rdv/table/' + table.id + '/remove_user/' + user, {headers: headers});
    }

    addGameToTable(table: Table, game: number): Observable<any> {
        const headers = new HttpHeaders().set('Authorization', 'Bearer ' + localStorage.getItem('token'));
        return this.httpClient.put(environment.apiBase + '/v1/rdv/table/' + table.id + '/add_game/' + game, null, {headers: headers});
    }

    removeGameFromTable(table: Table, game: number): Observable<any> {
        const headers = new HttpHeaders().set('Authorization', 'Bearer ' + localStorage.getItem('token'));
        return this.httpClient.delete(environment.apiBase + '/v1/rdv/table/' + table.id + '/remove_game/' + game, {headers: headers});
    }
}