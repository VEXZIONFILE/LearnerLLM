from learner_api.schemas import BuiltinSubject


class SubjectClassifier:
  MATH_KEYWORDS = {
      "equation", "solve", "algebra", "geometry", "fraction", "percent",
      "multiply", "divide", "add", "subtract", "variable", "graph", "theorem",
  }
  SCIENCE_KEYWORDS = {
      "atom", "molecule", "cell", "photosynthesis", "gravity", "force",
      "energy", "chemical", "biology", "physics", "experiment", "hypothesis",
  }
  ENGLISH_KEYWORDS = {
      "essay", "grammar", "verb", "noun", "paragraph", "thesis", "literature",
      "poem", "writing", "sentence", "punctuation", "analyze", "theme",
  }
  HISTORY_KEYWORDS = {
      "war", "revolution", "empire", "century", "president", "civilization",
      "timeline", "ancient", "colony", "treaty", "dynasty",
  }
  GEOGRAPHY_KEYWORDS = {
      "continent", "country", "climate", "map", "latitude", "longitude",
      "river", "mountain", "population", "region", "ecosystem",
  }

  def classify(self, text: str) -> BuiltinSubject:
      normalized = text.lower()
      scores = {
          BuiltinSubject.MATH: self._count_matches(normalized, self.MATH_KEYWORDS),
          BuiltinSubject.SCIENCE: self._count_matches(normalized, self.SCIENCE_KEYWORDS),
          BuiltinSubject.ENGLISH: self._count_matches(normalized, self.ENGLISH_KEYWORDS),
          BuiltinSubject.HISTORY: self._count_matches(normalized, self.HISTORY_KEYWORDS),
          BuiltinSubject.GEOGRAPHY: self._count_matches(normalized, self.GEOGRAPHY_KEYWORDS),
      }
      best = max(scores, key=scores.get)
      return best if scores[best] > 0 else BuiltinSubject.GENERAL

  @staticmethod
  def _count_matches(text: str, keywords: set[str]) -> int:
      return sum(1 for keyword in keywords if keyword in text)
