package MindStore.command.personDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
public class UserDto extends PersonDto implements Serializable {

    @NotEmpty
    @Size(min = 5, max = 40, message = "Address should have at least 5 characters")
    private String address;

    @NotNull //qd datetimeformat em vez de notempty
    @DateTimeFormat
    private LocalDate dateOfBirth;
}
