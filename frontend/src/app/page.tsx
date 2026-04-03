"use client";

import { Client } from "@stomp/stompjs";
import { useEffect, useMemo, useRef, useState } from "react";
import SockJS from "sockjs-client";

type TranscriptItem = {
  id: string;
  speaker: string;
  text: string;
  createdAt: string;
};

type SpeechRecognition = {
  continuous: boolean;
  interimResults: boolean;
  lang: string;
  onresult: ((event: any) => void) | null;
  onend: (() => void) | null;
  onerror: ((event: any) => void) | null;
  start: () => void;
  stop: () => void;
};

type SpeechRecognitionConstructor = new () => SpeechRecognition;

const apiBase = process.env.NEXT_PUBLIC_API_BASE || "http://localhost:8080";

export default function Home() {
  const [meetingTitle, setMeetingTitle] = useState("Weekly Sync");
  const [meetingId, setMeetingId] = useState<string | null>(null);
  const [email, setEmail] = useState("");
  const [speaker, setSpeaker] = useState("You");
  const [line, setLine] = useState("");
  const [transcripts, setTranscripts] = useState<TranscriptItem[]>([]);
  const [status, setStatus] = useState("idle");
  const [speechSupported, setSpeechSupported] = useState(true);
  const [isListening, setIsListening] = useState(false);
  const [interimText, setInterimText] = useState("");
  const [socketStatus, setSocketStatus] = useState("disconnected");
  const recognitionRef = useRef<SpeechRecognition | null>(null);
  const stompRef = useRef<Client | null>(null);

  const canJoin = meetingTitle.trim().length > 0 && email.trim().length > 3;
  const canSend = meetingId && line.trim().length > 0;

  const meetingBadge = useMemo(() => {
    if (!meetingId) return "Not started";
    return `Meeting ID: ${meetingId}`;
  }, [meetingId]);

  async function createMeeting() {
    setStatus("creating");
    const response = await fetch(`${apiBase}/api/meetings`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ title: meetingTitle }),
    });
    const data = await response.json();
    setMeetingId(data.id);
    setStatus("created");
  }

  async function joinMeeting() {
    if (!meetingId) return;
    await fetch(`${apiBase}/api/meetings/${meetingId}/join`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, displayName: speaker }),
    });
    setStatus("joined");
  }

  async function sendTranscriptText(text: string) {
    if (!meetingId) return false;
    const payload = {
      speaker,
      text,
      createdAt: new Date().toISOString(),
    };
    const response = await fetch(
      `${apiBase}/api/meetings/${meetingId}/transcripts`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      }
    );
    if (response.ok) {
      if (socketStatus !== "connected") {
        setTranscripts((prev) => [
          { id: crypto.randomUUID(), ...payload },
          ...prev,
        ]);
      }
      return true;
    }
    return false;
  }

  async function sendLine() {
    if (!meetingId) return;
    const success = await sendTranscriptText(line);
    if (success) {
      setLine("");
    }
  }

  useEffect(() => {
    const SpeechRecognitionCtor =
      (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;
    if (!SpeechRecognitionCtor) {
      setSpeechSupported(false);
      return;
    }

    const recognition = new (SpeechRecognitionCtor as SpeechRecognitionConstructor)();
    recognition.continuous = true;
    recognition.interimResults = true;
    recognition.lang = "en-US";

    recognition.onresult = (event) => {
      let interim = "";
      const finalSegments: string[] = [];

      for (let i = event.resultIndex; i < event.results.length; i += 1) {
        const result = event.results[i];
        const text = result[0]?.transcript?.trim();
        if (!text) continue;
        if (result.isFinal) {
          finalSegments.push(text);
        } else {
          interim += `${text} `;
        }
      }

      if (interim.trim().length > 0) {
        setInterimText(interim.trim());
      }

      if (finalSegments.length > 0) {
        setInterimText("");
        void sendTranscriptText(finalSegments.join(" "));
      }
    };

    recognition.onend = () => {
      setIsListening(false);
      setInterimText("");
    };

    recognition.onerror = () => {
      setIsListening(false);
      setInterimText("");
    };

    recognitionRef.current = recognition;

    return () => {
      recognition.stop();
      recognitionRef.current = null;
    };
  }, [meetingId, speaker]);

  useEffect(() => {
    if (!meetingId) {
      setSocketStatus("disconnected");
      return;
    }

    const client = new Client({
      webSocketFactory: () => new SockJS(`${apiBase}/ws`),
      reconnectDelay: 4000,
      onConnect: () => {
        setSocketStatus("connected");
        client.subscribe(
          `/topic/meetings/${meetingId}/transcripts`,
          (message) => {
            try {
              const event = JSON.parse(message.body) as TranscriptItem;
              setTranscripts((prev) => {
                if (prev.some((item) => item.id === event.id)) {
                  return prev;
                }
                return [event, ...prev];
              });
            } catch {
              // Ignore malformed messages.
            }
          }
        );
      },
      onDisconnect: () => {
        setSocketStatus("disconnected");
      },
      onWebSocketClose: () => {
        setSocketStatus("disconnected");
      },
      onStompError: () => {
        setSocketStatus("error");
      },
    });

    client.activate();
    stompRef.current = client;

    return () => {
      client.deactivate();
      stompRef.current = null;
      setSocketStatus("disconnected");
    };
  }, [meetingId]);

  function startListening() {
    if (!meetingId || !recognitionRef.current) return;
    setIsListening(true);
    recognitionRef.current.start();
  }

  function stopListening() {
    if (!recognitionRef.current) return;
    recognitionRef.current.stop();
    setIsListening(false);
    setInterimText("");
  }

  async function endMeeting() {
    if (!meetingId) return;
    setStatus("ending");
    await fetch(`${apiBase}/api/meetings/${meetingId}/end`, {
      method: "POST",
    });
    setStatus("ended");
  }

  return (
    <main>
      <div className="container">
        <section className="hero">
          <span className="badge">{meetingBadge}</span>
          <h1>Meet Assistant</h1>
          <p>
            Host a live meeting, stream transcripts, and let Gemini craft a
            summary, action items, and analysis. When the meeting ends, everyone
            gets the email.
          </p>
        </section>

        <section className="grid">
          <div className="card">
            <span className="label">Meeting setup</span>
            <input
              className="input"
              value={meetingTitle}
              onChange={(event) => setMeetingTitle(event.target.value)}
              placeholder="Meeting title"
            />
            <div className="actions">
              <button className="button" onClick={createMeeting}>
                Start meeting
              </button>
              <button
                className="button secondary"
                disabled={!meetingId}
                onClick={endMeeting}
              >
                End meeting
              </button>
            </div>
          </div>

          <div className="card">
            <span className="label">Join</span>
            <input
              className="input"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              placeholder="you@company.com"
            />
            <input
              className="input"
              value={speaker}
              onChange={(event) => setSpeaker(event.target.value)}
              placeholder="Display name"
            />
            <button className="button" disabled={!canJoin} onClick={joinMeeting}>
              Join meeting
            </button>
          </div>

          <div className="card">
            <span className="label">Transcript line</span>
            <textarea
              className="textarea"
              value={line}
              onChange={(event) => setLine(event.target.value)}
              placeholder="Paste or type transcript line here"
            />
            <div className="actions">
              <button className="button" disabled={!canSend} onClick={sendLine}>
                Send line
              </button>
              <button
                className="button secondary"
                disabled={!meetingId || !speechSupported}
                onClick={isListening ? stopListening : startListening}
              >
                {isListening ? "Stop live transcription" : "Start live transcription"}
              </button>
            </div>
            {!speechSupported && (
              <span className="label">Live transcription not supported</span>
            )}
            {interimText && (
              <span className="label">Listening: {interimText}</span>
            )}
            <span className="label">Status: {status}</span>
            <span className="label">Live updates: {socketStatus}</span>
          </div>
        </section>

        <section className="card">
          <span className="label">Latest transcript</span>
          <ul className="list">
            {transcripts.length === 0 && (
              <li className="label">No transcript lines yet</li>
            )}
            {transcripts.map((item) => (
              <li key={item.id}>
                <strong>{item.speaker}:</strong> {item.text}
              </li>
            ))}
          </ul>
        </section>
      </div>
    </main>
  );
}
