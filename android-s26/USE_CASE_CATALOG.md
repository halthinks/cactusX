# CactusX use-case catalog for Galaxy S26 Ultra

Source basis: Cactus Needle 3 tool-calling/extraction docs, Whistle speech model documentation, Mirage structured vision documentation, and Android 16 platform restrictions. This is an **opportunity inventory**, not a claim that all integrations have been implemented or validated.

## Capability primitives

- **N** (Needle): intent classification, registered tool selection, argument generation, text-to-schema.
- **W** (Whistle): speech recognition for English, German, French, Spanish, Italian, Dutch, Polish; word timestamps, keyword biasing, embeddings.
- **M** (Mirage): scene description, object labels/attributes, boxes/masks, OCR, relationships, constrained form extraction.
- **A**: Additional Android API, trusted local data, external system, or hardware integration required.
- **V**: Independent validation required for safety, identity, exact identification, or high-stakes decisions.

## Use-case families

### Personal device (N + A)
App launch; Settings navigation; battery/status queries; flashlight control; share-sheet preparation; timers; alarms; reminders; calendar drafting; SMS composition; dialer prefill; contacts lookup; approved file operations; notification triage with explicit notification access; accessibility shortcuts; device orientation automation; media playback controls; search among indexed local data; phone-to-PC command relay.

### Speech (W, optionally N/A)
Dictation; spoken task capture; voice search; local transcription; meeting capture; interview notes; spoken diaries; captions; timed transcripts; voice notes; hands-free task creation; field notes; voice-form entry; terminology-biased recognition; multilingual speech identification among the seven supported languages; audio search indexing; transcript-based summaries by an additional model; voice-controlled app intents through Needle; speech embedding comparison as an experimental downstream classifier.

### Vision (M, optionally N/A)
Camera scene description; alt text; object boxes; masks; simple counting; colors/material/shape recognition; OCR from labels/signs/documents; product packaging read; visual relation extraction; visible action/event labeling; constrained image forms; screen-reading assist; object selection; image categorization; visual inventory intake; document triage; simple visual QA; photo metadata creation; catalog candidate lookup; face-blind person description without identity claims; local search by visible characteristics; visible plant or pest observations; visible tool/component observation.

### Household and lifestyle (W + M + N + A)
Shopping lists; pantry labels; expiry-date OCR candidates; recipe ingredient inventory; repair project logs; home maintenance diaries; smart-home commands; appliance manual lookup; room/object inventories; garage organization; moving-box labels; gardening observations; family schedule preparation; pet photo logs; accessibility descriptions; car maintenance photos; hiking notes; travel notes; offline sign reading.

### Business and operations (W + M + N + A)
Shelf inventory; barcode/label OCR candidate capture; stock counts; picking verification; receiving logs; jobsite notes; timesheet dictation; incident reports; form classification; receipt field extraction candidates; asset tracking; CRM intake drafts; purchase-order fields; warehouse zone descriptions; maintenance tickets; support intent routing; customer-call transcripts with consent; appointment routing; document filing; quality inspection candidate flags; compliance checklist intake (not compliance certification).

### Engineering and making (W + M + N + A/V)
PCB label OCR; board visual observations; component/package candidate identification; BOM intake drafts; wiring annotation; test-bench notes; issue reproduction via voice; maintenance inspection logs; datasheet lookup routing; CAD command intent routing; design-review capture; robot instruction routing to safety controller; telemetry query routing; sensor installation labels; tool crib catalog; 3D-print part photo catalog; project documentation; fastener candidate counting; construction framing/photo progress; infrastructure project logs; electrical safety observations **not certification**.

### Pokémon and retail (M + N + A/V)
Read visible booster-box labels; set-name candidate extraction; inventory counts; stock intake; photo catalog; edition candidate lookup; product sorting; shelf organization; listing drafts; price-lookup request routing; condition observation (not grading); authenticity candidate anomalies (not certification); sales order intake; supplier records; item-location retrieval.

### Robotics, IoT and embedded (W/M/N + A/V)
Hands-free commands; object detection to control-loop inputs; visual scene logs; robotics UI; smart-home hubs; access-controlled actuation; anomaly event candidates; environmental scene tags; warehouse robot observation; wearable or glasses perception; car cabin command routing; inspection drone metadata. All actuation must be verified with independent safety mechanisms.

### Education and accessibility (W/M/N + A)
Spoken note capture; flashcard source extraction; textbook OCR candidate; classroom captions; visual descriptions; structured quiz answers via external content; study scheduling; object naming for young learners; hands-free accessibility; navigation descriptions from offline map/system; reading labels aloud via TTS; screenshot guidance; language practice for the seven documented speech languages; guided project checklists.

## Explicit non-capabilities

No automatic trustworthy general reasoning, identity verification, medical diagnosis, legal conclusions, precise physical measurement, live financial data, speaker diarization, speech synthesis from Whistle, native Khmer transcription, unrestricted OS control, background microphone access, or guaranteed NPU acceleration. These need other systems, evidence and/or permissions.

## Build phases

1. On-device structured tool approval and two safe adapters.
2. Phone task adapters (reminder, call draft, sharing) with per-action confirmation and negative tests.
3. Vision/OCR record viewer and selection overlay.
4. Dictation/timestamp UX and longer recording with bounded windows.
5. Separate local index/archive and opted-in history encryption strategy.
6. Personal workflows, then specialized engineering/retail integrations.
7. Physical-device performance, privacy, robustness, and accessibility acceptance.

## Sources

- https://www.cactuscompute.com/needle
- https://www.cactuscompute.com/blog/whistle
- https://cactuscompute.com/blog/mirage
- https://www.cactuscompute.com/blog/needle-supported-devices
- https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start
