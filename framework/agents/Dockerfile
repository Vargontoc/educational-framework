FROM ollama/ollama:latest

WORKDIR /agents

COPY ai-educational-child/Modelfile              ./ai-educational-child/Modelfile
COPY ai-educational-child/context/               ./ai-educational-child/context/
COPY ai-educational-child/personalities/         ./ai-educational-child/personalities/
COPY entrypoint.sh                               ./entrypoint.sh

RUN chmod +x ./entrypoint.sh

EXPOSE 11434

ENTRYPOINT ["./entrypoint.sh"]
