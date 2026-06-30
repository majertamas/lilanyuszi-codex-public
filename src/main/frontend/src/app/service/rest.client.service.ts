import {inject} from '@angular/core';
import {HttpClient, HttpErrorResponse, HttpEvent, HttpEventType, HttpParams, HttpResponse} from '@angular/common/http';
import {catchError, map, Observable, throwError} from 'rxjs';
import {RestResponse} from '../model/rest.response.model';
import {MessageService, ToastMessageOptions} from 'primeng/api';
import {Message} from '../model/message.model';
import {BlockUiService} from './block-ui.service';
import {Router} from '@angular/router';

export abstract class RestClientService {

  private readonly httpClient: HttpClient = inject(HttpClient);
  private readonly messageService: MessageService = inject(MessageService);
  private readonly blockUiService: BlockUiService = inject(BlockUiService);
  private readonly router: Router = inject(Router);

  post<T>(path: string, body: any, params?: object): Observable<T> {
    this.blockUiService.blocked.set(true);
    let options: any = {
      observe: 'response',
    };
    if (params) {
      options.params = params;
    }
    const withoutMessage: boolean | undefined = params && (params as any)['withoutMessage'] === true;
    let httpEvent: Observable<HttpEvent<RestResponse<T>>> = this.httpClient.post<RestResponse<T>>(path, body, options);
    return this.processHttpEvent<T>(this.toResponse(httpEvent), withoutMessage);
  }

  get<T>(url: string, params?: object): Observable<T> {
    this.blockUiService.blocked.set(true);
    let options: any = {
      observe: 'response',
      params: params
    };
    const withoutMessage: boolean | undefined = params && (params as any)['withoutMessage'] === true;
    let httpEvent: Observable<HttpEvent<RestResponse<T>>> = this.httpClient.get<RestResponse<T>>(url, options);
    return this.processHttpEvent<T>(this.toResponse(httpEvent), withoutMessage);
  }

  put<T>(path: string, body: any, params?: object): Observable<T> {
    this.blockUiService.blocked.set(true);
    const options: any = {
      observe: 'response'
    };
    const withoutMessage: boolean | undefined = params && (params as any)['withoutMessage'] === true;
    let httpEvent: Observable<HttpEvent<RestResponse<T>>> = this.httpClient.put<RestResponse<T>>(path, body, options);
    return this.processHttpEvent<T>(this.toResponse(httpEvent), withoutMessage);
  }

  delete<T>(path: string, params?: HttpParams): Observable<T> {
    this.blockUiService.blocked.set(true);
    let options: any = {
      observe: 'response',
      params: params
    };
    const withoutMessage: boolean | undefined = params && (params as any)['withoutMessage'] === true;
    let httpEvent: Observable<HttpEvent<RestResponse<T>>> = this.httpClient.delete<RestResponse<T>>(path, options);
    return this.processHttpEvent<T>(this.toResponse(httpEvent), withoutMessage);
  }

  private toResponse<T>(httpEvent: Observable<HttpEvent<RestResponse<T>>>): Observable<HttpResponse<RestResponse<T>>> {
    return httpEvent.pipe(
      map((event: HttpEvent<RestResponse<T>>): HttpResponse<RestResponse<T>> => {
        if (event.type === HttpEventType.Response) {
          return event;
        }
        throw new Error('Not an HttpResponse');
      })
    )
  }

  private processHttpEvent<T>(event: Observable<HttpResponse<RestResponse<T>>>, withoutMessage: boolean | undefined): Observable<T> {
    return event
      .pipe(
        map((response: HttpResponse<RestResponse<T>>): T => {
          return this.responseHandler<T>(response);
        }),
        catchError((error: HttpErrorResponse): Observable<T> => {
          return this.errorHandler<T>(error, withoutMessage);
        })
      );
  }

  private responseHandler<T>(response: HttpResponse<RestResponse<T>>): T {
    const body: RestResponse<T> | null = response.body
    if (!body) {
      throw new Error('Response body is null');
    }

    console.log(body)

    if (body.messages && body.messages.length > 0) {
      body.messages.forEach((message: Message): void => {
        const toastMessage: ToastMessageOptions = {
          detail: message.text,
          severity: message.severity.toLowerCase(),
          summary: message.severity,
          life: 5000
        };
        this.messageService.add(toastMessage);
      });
    }
    this.blockUiService.blocked.set(false);
    return body.data;
  }

  private errorHandler<T>(error: HttpErrorResponse, withoutMessage: boolean | undefined): Observable<T> {
    if (!withoutMessage) {
      this.messageService.add(
        {
          severity: 'error',
          summary: `Error ${error.status}`,
          detail: error.message,
          life: 5000
        }
      );
    }
    this.blockUiService.blocked.set(false);
    if (error.status === 401) {
      this.router.navigate(['/login']);
    }
    return throwError((): Error => new Error(error.message));
  }
}
