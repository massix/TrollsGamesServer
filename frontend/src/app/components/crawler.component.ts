import { Component, OnInit } from '@angular/core';
import { Game } from '../data/game';
import { CrawlService } from '../services/crawl.service';
import { AlertService } from '../services/alert.service';
import { Queue } from '../data/queue';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { SimpleTimer } from 'ng2-simple-timer';
import { Router, NavigationStart } from '@angular/router';

@Component({
    templateUrl: '../views/crawler.component.html',
    styleUrls: ['../styles/crawler.component.css']
})
export class CrawlerComponent implements OnInit {
    queues: Queue[];

    getAllQueues() {
        this.crawlService.getQueues().subscribe(
            data => this.queues = data
        );
    }

    updateQueues() {
        if (this.queues == null) return;
        
        this.queues.forEach(q => {
            if (q.running) {
                console.log('updating queue ' + q.queue);
                this.crawlService.getQueue(q.queue).subscribe(newQueue => {
                    q.total = newQueue.total;
                    q.crawled = newQueue.crawled;
                    q.cacheHit = newQueue.cacheHit;
                    q.cacheMiss = newQueue.cacheMiss;
                    q.running = newQueue.running;
                    q.finished = newQueue.finished;
                });
            }
        });
    }

    ngOnInit() {
        this.getAllQueues();
        this.timerService.newTimer('5sec', 10);
        this.timerService.subscribe('5sec', () => this.updateQueues());

        this.router.events.subscribe(event => {
            if (event instanceof NavigationStart) {
                console.log('Unsubscribe timer');
                this.timerService.unsubscribe('5sec');
            }
        });
    }

    removeQueue(queue: Queue) {
        this.crawlService.deleteQueue(queue.queue).subscribe(
            data => this.getAllQueues(),
            (error: HttpErrorResponse) => {
                if (error.status === 200) {
                    this.getAllQueues();
                } else {
                    this.alertService.error('Error ' + error.status + ' - ' + error.error);
                }
            }
        );
    }

    constructor(private crawlService: CrawlService, 
        private alertService: AlertService, 
        private timerService: SimpleTimer,
        private router: Router) {}
}