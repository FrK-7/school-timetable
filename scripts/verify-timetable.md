# Prompt di Verifica Orario con AI

Questo file contiene un prompt strutturato da fornire a un modello di intelligenza artificiale (Claude, GPT-4, ecc.) insieme al file Excel dell'orario generato e all'elenco dei vincoli configurati. L'AI verificherà il rispetto dei vincoli e suggerirà ottimizzazioni.

---

## Come usare

1. Apri un modello AI che supporti l'analisi di file Excel (es. Claude con allegati, ChatGPT con Code Interpreter)
2. Allega il file `orario.xlsx` generato dal sistema
3. Copia e incolla il prompt sottostante, compilando la sezione VINCOLI con quelli effettivamente configurati nella tua scuola

---

## PROMPT DA COPIARE

```
Sei un esperto di organizzazione scolastica italiana. Ti ho allegato un file Excel con l'orario settimanale di una scuola media. Il file ha questo formato:

STRUTTURA EXCEL:
- Colonna A: raggruppamento per materia (righe gialle = intestazione materia) e nome docente
- Colonna B: riepilogo classe(ore) per ogni docente (es. "1A(4h) 2B(3h)")
- Colonne C in poi: griglia giornaliera. Ogni giorno ha N sotto-colonne (una per ora). Il contenuto delle celle è il nome della classe assegnata a quell'ora.
- I giorni sono: LUN, MAR, MER, GIO, VEN (o anche SAB se 6 giorni)
- Le ore sono numerate da 1 a N (tipicamente 6)

CONFIGURAZIONE SCUOLA:
- Giorni settimanali: [INSERISCI: 5 o 6]
- Ore giornaliere: [INSERISCI: es. 6]
- Plessi: [INSERISCI: elenco plessi e quali classi appartengono a ciascuno, es. "Plesso A: 1A,1B,2A,2B / Plesso B: 3A,3B"]

VINCOLI OBBLIGATORI (devono essere rispettati al 100%):
[INSERISCI ELENCO, es:]
- Un docente non può essere in due classi alla stessa ora
- Una classe non può avere due docenti alla stessa ora
- Le ore nella stessa classe per lo stesso docente devono essere consecutive (no rientri)
- Docenti con classi in plessi diversi non possono avere ore consecutive in plessi diversi (serve almeno 1 ora di stacco per spostamento)
- [Docente X] ha il giorno libero il [GIORNO]
- [Docente Y] non è disponibile il [GIORNO] dalle ore [N] alle ore [M]
- [Docente Z] deve fare almeno 2 ore consecutive per classe

VINCOLI PREFERENZIALI (da rispettare il più possibile, ma violabili):
[INSERISCI ELENCO, es:]
- Minimizzare le ore di buco per ogni docente
- Minimizzare i cambi di plesso nella giornata
- Raggruppare le ore dello stesso plesso in blocchi consecutivi
- [Docente W] preferisce il giorno libero il [GIORNO]

MONTE ORE ASSEGNATO:
[INSERISCI per ogni docente, es:]
- Mario Rossi: Italiano → 1A(4h), 1B(4h), 2A(4h)
- Anna Verdi: Matematica → 1A(6h), 1B(6h)
- ...

---

COMPITO - FASE 1: VERIFICA (checklist)

Analizza l'Excel riga per riga e verifica OGNI vincolo. Produci un report strutturato:

### Checklist Vincoli Obbligatori

Per ogni vincolo obbligatorio, indica:
- ✅ RISPETTATO — con breve conferma
- ❌ VIOLATO — con dettaglio esatto (docente, giorno, ora, cosa non va)

### Checklist Vincoli Preferenziali

Per ogni vincolo preferenziale, indica:
- ✅ RISPETTATO — con breve conferma
- ⚠️ PARZIALMENTE VIOLATO — con dettaglio e quantificazione (es. "3 buchi su 5 giorni")
- ❌ FORTEMENTE VIOLATO — con dettaglio

### Verifica Monte Ore

Per ogni docente, verifica che il totale ore per classe nell'orario corrisponda al monte ore assegnato. Segnala discrepanze.

---

COMPITO - FASE 2: SUGGERIMENTI DI OTTIMIZZAZIONE

Dopo la verifica, proponi miglioramenti concreti che:
1. NON violino alcun vincolo obbligatorio
2. Riducano le violazioni dei vincoli preferenziali

Per ogni suggerimento:
- Descrivi lo SCAMBIO proposto (es. "Spostare Rossi da LUN ora 3 a MER ora 2")
- Spiega PERCHÉ migliora (es. "Elimina un buco di 2 ore per Rossi")
- Verifica che lo scambio non crei NUOVE violazioni obbligatorie
- Quantifica il miglioramento (es. "Da 4 buchi a 2 buchi per quel docente")

Ordina i suggerimenti dal più impattante al meno impattante.

---

COMPITO - FASE 3: ITERAZIONE

Se hai proposto scambi nella Fase 2, applica mentalmente i migliori (quelli che non creano violazioni) e ripeti la Fase 1 sull'orario modificato. Continua fino a quando:
- Non ci sono più violazioni obbligatorie, E
- Non trovi ulteriori scambi che migliorino i vincoli preferenziali senza violare quelli obbligatori

Produci l'orario finale ottimizzato in formato tabellare:
DOCENTE | LUN1 | LUN2 | ... | VEN6

---

NOTE IMPORTANTI PER L'AI:
- Sii RIGOROSO nella verifica: controlla ogni singola cella, non campionare
- I plessi sono importanti: due classi nello stesso plesso possono avere ore consecutive, due in plessi diversi NO
- "Ore consecutive" significa ora N e ora N+1 nello STESSO giorno
- Un "buco" è uno slot vuoto tra due ore di lavoro nello stesso giorno
- Se un docente ha solo 1 ora in una classe, il vincolo "minimo 2 ore consecutive" NON si applica (non è possibile fare 2h con solo 1h assegnata)
- Nella Fase 2, proponi solo scambi FATTIBILI: non puoi mettere un docente dove ha già un'altra classe
```

---

## Uso avanzato: verifica automatica con API

Se vuoi automatizzare la verifica, puoi usare l'API di Claude/OpenAI programmaticamente:

```python
import anthropic
import base64

client = anthropic.Anthropic()

with open("orario.xlsx", "rb") as f:
    excel_data = base64.standard_b64encode(f.read()).decode("utf-8")

# Leggi i vincoli dal sistema (oppure inseriscili manualmente)
vincoli = """
[Incolla qui i vincoli dalla tua configurazione]
"""

prompt = f"""
[Incolla il prompt sopra, sostituendo i placeholder con i vincoli reali]
{vincoli}
"""

message = client.messages.create(
    model="claude-sonnet-4-6-20250514",
    max_tokens=8000,
    messages=[
        {
            "role": "user",
            "content": [
                {
                    "type": "document",
                    "source": {
                        "type": "base64",
                        "media_type": "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        "data": excel_data,
                    },
                },
                {
                    "type": "text",
                    "text": prompt
                }
            ],
        }
    ],
)

print(message.content[0].text)
```

---

## Output atteso

Il modello AI produrrà:
1. Una checklist con stato di ogni vincolo (✅/❌/⚠️)
2. Una lista di scambi suggeriti ordinati per impatto
3. (Opzionale) Un orario rielaborato in formato tabellare

Questo permette di:
- Validare rapidamente un orario generato
- Identificare violazioni che il solver non è riuscito a risolvere
- Ottenere suggerimenti di micro-ottimizzazione manuali
- Iterare fino a convergenza su un orario ottimale
