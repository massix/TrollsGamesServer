import { Component, OnInit } from '@angular/core';
import { StatsService } from '../services/stats.service';
import { ServerInformation } from '../data/serverinformation';
import { Stats } from '../data/stats';

@Component({
    template: 
    `
        <div class="container">
            <div *ngIf="serverInformation" class="row">
                <div class="col-md-4 col-sm-12">Server version: {{serverInformation.version}}</div>
                <div class="col-md-4 col-sm-12">Build timestamp: {{serverInformation.timestamp}}</div>
                <div class="col-md-4 col-sm-12">Artifact name: {{serverInformation.artifact}}</div>
            </div>

            <div *ngIf="stats" class="row table">
                <table class="table table-condensed table-hover">
                    <thead>
                        <tr>
                            <th scope="col">Hash</th>
                            <th scope="col">Endpoint</th>
                            <th scope="col">Counter</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr *ngFor="let stat of stats">
                            <th scope="col">{{stat.hashedUser}}</th>
                            <td>{{stat.endpoint}}</td>
                            <td>{{stat.counter}}</td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    `
})
export class InformationComponent implements OnInit {
    serverInformation: ServerInformation;
    stats: Stats[];
    
    constructor(private statsService: StatsService) {}

    ngOnInit() {
        this.statsService.getServerInformation().subscribe(data => this.serverInformation = data);
        this.statsService.getServerStatistics().subscribe(data => this.stats = data);
    }
}