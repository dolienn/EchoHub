import { Injectable } from '@angular/core';
import {MediaUploaderService} from "./media-uploader.service";

@Injectable({
  providedIn: 'root'
})
export class AudioRecorderService {
  isRecording: boolean = false;
  mediaRecorder: MediaRecorder | null = null;
  audioChunks: Blob[] = [];
  audioUrl: string = '';

  constructor(private mediaUploaderService: MediaUploaderService) { }

  toggleRecording(): void {
    if (this.isRecording) {
      this.stopRecording();
    } else {
      this.startRecording();
    }
  }

  private startRecording(): void {
    if (this.isRecording) {
      console.warn('Recording is already in progress.');
      return;
    }

    this.requestAudioPermission()
      .then((stream) => {
        this.setupMediaRecorder(stream);
        this.startMediaRecorder();
      })
      .catch((err) => {
        this.handleError(err);
      });
  }

  private async requestAudioPermission(): Promise<MediaStream> {
    try {
      return await navigator.mediaDevices.getUserMedia({ audio: true });
    } catch (err) {
      throw new Error('Failed to access the microphone');
    }
  }

  private setupMediaRecorder(stream: MediaStream): void {
    this.mediaRecorder = new MediaRecorder(stream);
    this.mediaRecorder.ondataavailable = (event: BlobEvent) => {
      this.audioChunks.push(event.data);
    };

    this.mediaRecorder.onstop = this.onRecordingStop;
  }

  private startMediaRecorder(): void {
    if (this.mediaRecorder) {
      this.mediaRecorder.start();
      this.isRecording = true;
    }
  }

  private onRecordingStop = (): void => {
    const audioBlob = new Blob(this.audioChunks, { type: 'audio/mp3' });
    this.audioUrl = URL.createObjectURL(audioBlob);

    const file = new File([audioBlob], 'audio.mp3', { type: 'audio/mp3' });
    this.uploadFile(file);
  };

  private uploadFile(file: File): void {
    const reader = new FileReader();
    reader.onload = () => {
      this.mediaUploaderService.uploadMediaContent(reader.result, file);
      this.resetRecordingState();
    }
    reader.readAsDataURL(file);
  }

  private resetRecordingState(): void {
    this.audioChunks = [];
    this.audioUrl = '';
  }

  private handleError(error: Error): void {
    console.error('Error occurred:', error.message);
    alert('There was an issue accessing the microphone.');
  }

  private stopRecording(): void {
    if (this.mediaRecorder) {
      this.mediaRecorder.stop();
    }
    this.isRecording = false;
  }
}

