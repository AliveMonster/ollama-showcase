This project is a Spring Boot application demonstrating how to build enterprise-grade AI capabilities entirely locally 
using **Spring AI 2.0.1** and **Ollama (Llama 3.2)**. 

By running locally, this setup guarantees **zero API costs** and **100% data privacy** (no data ever leaves your machine).

## Prerequisites
1. **Java 17+** installed.
2. **Ollama** installed on your machine (ollama.com).
3. Pull the Llama 3.2 model by running this in your terminal:
   `ollama pull llama3.2`
4. **PDF Setup:** Ensure you have placed a readable text PDF named `runbook.pdf` inside `src/main/resources/` for the RAG feature to work.

## How to Run
You can run the application directly from your IDE (like IntelliJ IDEA) by hitting the green Play button on `OllamaShowcaseApplication.java`, 
or via the terminal using Gradle:
`./gradlew bootRun`
*Note: On startup, the application will pause for a few seconds to ingest and vectorize `runbook.pdf` into the in-memory SimpleVectorStore.*

---

## Capabilities & Endpoints

### 1. Structured Data Extraction (Zero-Shot)
Takes unstructured human text (like a customer complaint) and forces the LLM to map it into a strict JSON Java Record.

* **URL:** `POST http://localhost:8080/api/disputes/autofill`
* **Headers:** `Content-Type: text/plain`
* **Body (Raw Text):**
  `I am writing to dispute a charge on my credit card. On March 3rd, I was at Starbucks in Chicago during a business trip.
  I bought a coffee for $14.50. However, when I checked my statement today, I saw that I was charged twice for that exact amount on the same day.
  I haven't contacted them yet. Please fix this duplicate charge.`
* **Expected Response:** Valid JSON matching the `DisputeFormState` record.

### 2. Privacy-First RAG (Chat with your PDF)
Uses Spring AI's `QuestionAnswerAdvisor` to intercept a question, perform a similarity search against the ingested PDF, and inject the context before 
calling the LLM.

* **URL:** `GET http://localhost:8080/api/docs/ask?query=YOUR_QUESTION_HERE`
* **Example:** `GET http://localhost:8080/api/docs/ask?query=What is this document about?`
* **Expected Response:** JSON `RagResponse` containing the answer.

### 3. The DevOps Copilot (Live Log Analysis)
Simulates a deep, nested backend exception, writes it to a local log file (`logs/application.log`), and uses an AI Copilot to read the file, 
diagnose the root cause, and suggest a fix.

**Step A: Trigger the Crash**
* **URL:** `GET http://localhost:8080/api/payments/simulate-crash`
* **Action:** This will throw a NullPointerException and write a massive stack trace to your local log file.

**Step B: Analyze the Crash**
* **URL:** `GET http://localhost:8080/api/logs/analyze-recent`
* **Action:** The LLM reads the last 50 lines of the log file, categorizes the error severity, explains the root cause in plain English, and provides a fix.
* **Expected Response:** Valid JSON matching the `LogAnalysisResult` record.
EOF
