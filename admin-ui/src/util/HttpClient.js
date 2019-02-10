/* eslint-disable no-console */
import { toast } from 'react-toastify';

const ApiUrl = process.env.REACT_APP_API_URL;

class HttpClient {
  constructor(headers) {
    this.headers = headers;
  }

  async request({
    method, url, data, logSuccess, logError,
  }) {
    const settings = {
      method,
      headers: this.headers,
    };
    if (data) settings.body = JSON.stringify(data);
    const callInfo = `method: ${method}, url: ${url}, settings: ${JSON.stringify(settings)}`;

    console.log(settings);
    try {
      const res = await fetch(ApiUrl + url, settings);
      const json = await res.json();
      const response = { isOk: res.statusText === 'OK', data: json };
      if (logSuccess && response.isOk) toast.success(`Success: settings - ${callInfo}\n`);
      if (logError && !response.isOk) toast.error(`Error: settings - ${callInfo}\n, response - ${JSON.stringify(json)}`);
      console.log(callInfo + JSON.stringify(response));
      return response;
    } catch (error) {
      const response = { isOk: false, error };
      if (logError) toast.error(`Call Error - ${callInfo}`);
      console.log(callInfo + JSON.stringify(response));
      return response;
    }
  }

  async get(url, settings) {
    return this.request({ method: 'GET', url, ...settings });
  }

  async post(url, settings) {
    return this.request({ method: 'POST', url, ...settings });
  }

  async put(url, settings) {
    return this.request({ method: 'PUT', url, ...settings });
  }

  async delete(url, settings) {
    return this.request({ method: 'DELETE', url, ...settings });
  }
}

export default HttpClient;
