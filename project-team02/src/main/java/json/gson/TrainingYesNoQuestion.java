package json.gson;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class TrainingYesNoQuestion extends TrainingQuestion {

  @SerializedName("exact_answer")
  private String exactAnswer;

  protected TrainingYesNoQuestion(String id, String body, QuestionType type,
          List<String> documents, List<Snippet> snippets, List<String> concepts,
          List<Triple> triples, List<String> idealAnswer, String exactAnswer) {
    super(id, body, type, documents, snippets, concepts, triples, idealAnswer);
    this.exactAnswer = exactAnswer;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((exactAnswer == null) ? 0 : exactAnswer.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    TrainingYesNoQuestion other = (TrainingYesNoQuestion) obj;
    if (exactAnswer == null) {
      if (other.exactAnswer != null)
        return false;
    } else if (!exactAnswer.equals(other.exactAnswer))
      return false;
    return true;
  }

  public String getExactAnswer() {
    return exactAnswer;
  }

}
