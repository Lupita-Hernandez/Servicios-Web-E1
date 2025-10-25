package ws.beauty.salon.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ws.beauty.salon.dto.ReviewRequest;
import ws.beauty.salon.dto.ReviewResponse;
import ws.beauty.salon.model.Client;
import ws.beauty.salon.model.Review;
import ws.beauty.salon.repository.ClientRepository;
import ws.beauty.salon.repository.ReviewRepository;
import ws.beauty.salon.repository.ServiceRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository repository;
    private final ClientRepository clientRepository;
    private final ServiceRepository serviceRepository;
    private final ModelMapper modelMapper;

    /*@Override
    public List<ReviewResponse> findAll() {
        return repository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }*/

    @Override
    public List<ReviewResponse> findAllPaginated(int page, int pageSize) {
        PageRequest pageReq = PageRequest.of(page, pageSize);
        Page<Review> reviews = repository.findAll(pageReq);
        return reviews.getContent().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public ReviewResponse findById(Integer id) {
        Review review = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Review not found: " + id));
        return mapToResponse(review);
    }

    @Override
    public ReviewResponse create(ReviewRequest request) {
        Review review = mapToEntity(request);

        Client client = clientRepository.findById(request.getIdClient())
                .orElseThrow(() -> new EntityNotFoundException("Client not found: " + request.getIdClient()));
        review.setClient(client);

         serviceRepository.findById(request.getIdService())
                .orElseThrow(() -> new EntityNotFoundException("Service not found: " + request.getIdService()));
        review.setService(serviceRepository.findById(request.getIdService()).get());

        return mapToResponse(repository.save(review));
    }

    @Override
    public ReviewResponse update(Integer id, ReviewRequest request) {
        Review existing = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Review not found: " + id));

        modelMapper.map(request, existing);

        if (request.getIdClient() != null) {
            Client client = clientRepository.findById(request.getIdClient())
                    .orElseThrow(() -> new EntityNotFoundException("Client not found: " + request.getIdClient()));
            existing.setClient(client);
        }

        if (request.getIdService() != null) {
             serviceRepository.findById(request.getIdService())
                    .orElseThrow(() -> new EntityNotFoundException("Service not found: " + request.getIdService()));
            existing.setService(serviceRepository.findById(request.getIdService()).get());
        }

        return mapToResponse(repository.save(existing));
    }

    /*@Override
    public void delete(Integer id) {
        if (!repository.existsById(id))
            throw new EntityNotFoundException("Review not found: " + id);
        repository.deleteById(id);
    }*/

    @Override
    public List<ReviewResponse> findByClientId(Integer clientId) {
        return repository.findByClientId(clientId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<ReviewResponse> findByServiceId(Integer serviceId) {
        return repository.findByServiceId(serviceId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<ReviewResponse> findBySentiment(String sentiment) {
        return repository.findBySentiment(sentiment).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<ReviewResponse> findByRatingGreaterOrEqual(Integer rating) {
        return repository.findByRatingGreaterOrEqual(rating).stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ------------------- Métodos auxiliares -------------------

    private Review mapToEntity(ReviewRequest request) {
        return modelMapper.map(request, Review.class);
    }

    private ReviewResponse mapToResponse(Review review) {
        ReviewResponse response = modelMapper.map(review, ReviewResponse.class);
        if (review.getClient() != null) {
            response.setIdClient(review.getClient().getId());
            response.setFirstName(review.getClient().getFirstName());
        }
        if (review.getService() != null) {
            response.setIdService(review.getService().getId());
            response.setServiceName(review.getService().getServiceName());
        }
        return response;
    }
}

