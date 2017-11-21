import { Component, OnInit } from '@angular/core';
import { Game } from '../data/game';
import { CrawlService } from '../services/crawl.service';
import { AlertService } from '../services/alert.service';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { SimpleTimer } from 'ng2-simple-timer';
import { Router, NavigationStart } from '@angular/router';
import { CrawlerStatus } from '../data/crawlerstatus';

@Component({
    templateUrl: '../views/crawler.component.html',
    styleUrls: ['../styles/crawler.component.css']
})
export class CrawlerComponent implements OnInit {
    subscriptionId: string;
    crawlerStatus: CrawlerStatus;

    ngOnInit() {
        this.timerService.newTimer('3sec', 3);
        this.subscriptionId = this.timerService.subscribe('3sec', () => this.getStatus());

        this.router.events.subscribe(event => {
            if (event instanceof NavigationStart) {
                this.timerService.unsubscribe(this.subscriptionId);
            }
        });
    }

    getStatus() {
        this.crawlService.getStatus().subscribe(data => this.crawlerStatus = data);
    }

    wake() {
        this.crawlService.wake().subscribe(data => console.log(data), error => {});
    }

    constructor(private crawlService: CrawlService, 
        private alertService: AlertService, 
        private timerService: SimpleTimer,
        private router: Router) {}
}